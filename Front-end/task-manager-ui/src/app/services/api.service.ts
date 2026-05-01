import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  createUser(user: any) {
    return this.http.post(`${this.baseUrl}/users`, user);
  }

  getUsersByRole(role: string) {
    return this.http
      .get<ApiResponse<any[]>>(`${this.baseUrl}/users/role/${role}`)
      .pipe(map((res) => res.data || []));
  }

  createTask(task: any) {
    return this.http.post(`${this.baseUrl}/tasks`, task);
  }

  getTasksForTL(tlId: number) {
    return this.http
      .get<ApiResponse<any[]>>(`${this.baseUrl}/tasks/tl/${tlId}`)
      .pipe(map((res) => res.data || []));
  }

  createSubTask(subTask: any) {
    return this.http.post(`${this.baseUrl}/subtasks`, subTask);
  }

  getSubTasksForDeveloper(devId: number) {
    return this.http
      .get<ApiResponse<any[]>>(`${this.baseUrl}/subtasks/dev/${devId}`)
      .pipe(map((res) => res.data || []));
  }

  updateSubTaskStatus(subTaskId: number, status: string) {
    return this.http.put(`${this.baseUrl}/subtasks/${subTaskId}/status?status=${status}`, {});
  }
}
