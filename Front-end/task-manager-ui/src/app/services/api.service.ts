import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
    return this.http.get(`${this.baseUrl}/users/role/${role}`);
  }

  createTask(task: any) {
    return this.http.post(`${this.baseUrl}/tasks`, task);
  }

  getTasksForTL(tlId: number) {
  return this.http.get(`${this.baseUrl}/tasks/tl/${tlId}`);
}

createSubTask(subTask: any) {
  return this.http.post(`${this.baseUrl}/subtasks`, subTask);
}
}