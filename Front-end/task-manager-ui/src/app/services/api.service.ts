import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, shareReplay, tap, timeout } from 'rxjs';
import { environment } from '../../environments/environment';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  //private baseUrl = 'https://task-management-en0u.onrender.com';
  //private baseUrl = 'http://localhost:8080';

  private baseUrl = environment.apiUrl;
  private requestTimeoutMs = 15000;
  private usersByRoleCache = new Map<string, Observable<any[]>>();

  constructor(private http: HttpClient) {}

  clearUserCache() {
    this.usersByRoleCache.clear();
  }

  login(credentials: { email: string; password: string }) {
    return this.http
      .post<ApiResponse<any>>(`${this.baseUrl}/users/login`, credentials)
      .pipe(timeout(this.requestTimeoutMs));
  }

  createUser(user: any) {
    return this.http.post(`${this.baseUrl}/users`, user).pipe(
      timeout(this.requestTimeoutMs),
      tap(() => this.usersByRoleCache.clear()),
    );
  }

  getUsersByRole(role: string) {
    if (!this.usersByRoleCache.has(role)) {
      const request$ = this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/users/role/${role}`).pipe(
        timeout(this.requestTimeoutMs),
        map((res) => res.data || []),
        shareReplay(1),
      );

      this.usersByRoleCache.set(role, request$);
    }

    return this.usersByRoleCache.get(role)!;
  }

  // ── NEW: Get all users (used by User Management) ──────────────
  getAllUsers() {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/users`).pipe(
      timeout(this.requestTimeoutMs),
      map((res) => res.data || []),
    );
  }

  // ── NEW: Delete a user by ID ──────────────────────────────────
  deleteUser(userId: number) {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/users/${userId}`)
      .pipe(
        timeout(this.requestTimeoutMs),
        tap(() => this.usersByRoleCache.clear()),
      );
  }

  createTask(task: any) {
    return this.http.post(`${this.baseUrl}/tasks`, task).pipe(timeout(this.requestTimeoutMs));
  }

  getTasksForTL(tlId: number) {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/tasks/tl/${tlId}`).pipe(
      timeout(this.requestTimeoutMs),
      map((res) => res.data || []),
    );
  }

  getTasksForSupervisor(supervisorId: number) {
    return this.http
      .get<ApiResponse<any[]>>(`${this.baseUrl}/tasks/supervisor/${supervisorId}`)
      .pipe(
        timeout(this.requestTimeoutMs),
        map((res) => res.data || []),
      );
  }

  getTaskComments(taskId: number) {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/tasks/${taskId}/comments`).pipe(
      timeout(this.requestTimeoutMs),
      map((res) => res.data || []),
    );
  }

  addTaskComment(taskId: number, comment: string) {
    return this.http
      .post<ApiResponse<void>>(`${this.baseUrl}/tasks/${taskId}/comments`, { comment })
      .pipe(timeout(this.requestTimeoutMs));
  }

  createSubTask(subTask: any) {
    return this.http.post(`${this.baseUrl}/subtasks`, subTask).pipe(timeout(this.requestTimeoutMs));
  }

  getSubTasksForDeveloper(devId: number) {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/subtasks/dev/${devId}`).pipe(
      timeout(this.requestTimeoutMs),
      map((res) => res.data || []),
    );
  }

  updateSubTaskStatus(subTaskId: number, status: string) {
    return this.http
      .put(`${this.baseUrl}/subtasks/${subTaskId}/status?status=${encodeURIComponent(status)}`, {})
      .pipe(timeout(this.requestTimeoutMs));
  }

  changePassword(data: any) {
    return this.http
      .post<ApiResponse<any>>(`${this.baseUrl}/users/change-password`, data)
      .pipe(timeout(this.requestTimeoutMs));
  }
}
