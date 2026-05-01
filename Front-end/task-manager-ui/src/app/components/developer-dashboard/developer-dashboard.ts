import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-developer-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './developer-dashboard.html',
  styleUrl: './developer-dashboard.css'
})
export class DeveloperDashboardComponent implements OnInit {

  selectedDeveloperId = '';
  developers: any[] = [];
  subTasks: any[] = [];
  message = '';
  updatingSubTaskId = 0;

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadDevelopers();
  }

  loadDevelopers() {
    this.api.getUsersByRole('DEVELOPER').subscribe({
      next: (res: any) => {
        this.developers = res;

        if (this.developers.length > 0) {
          this.selectedDeveloperId = String(this.developers[0].id);
          this.loadSubTasks();
        }
      },
      error: (err) => {
        console.error('Error loading developers:', err);
        this.message = 'Could not load developers. Please check backend server.';
      }
    });
  }

  loadSubTasks() {
    if (!this.selectedDeveloperId) {
      this.subTasks = [];
      return;
    }

    this.api.getSubTasksForDeveloper(Number(this.selectedDeveloperId)).subscribe({
      next: (res: any) => {
        this.subTasks = res;
        this.message = '';
      },
      error: (err) => {
        console.error('Error loading subtasks:', err);
        this.subTasks = [];
        this.message = 'Could not load subtasks for selected developer.';
      }
    });
  }

  onDeveloperChange() {
    this.message = '';
    this.loadSubTasks();
  }

  markDone(subTask: any) {
    this.updatingSubTaskId = subTask.id;
    this.message = '';

    this.api.updateSubTaskStatus(subTask.id, 'DONE').subscribe({
      next: (res: any) => {
        this.message = res.message;
        this.loadSubTasks();
      },
      error: (err) => {
        console.error('Error updating subtask:', err);
        this.message = 'Could not update subtask status.';
      },
      complete: () => {
        this.updatingSubTaskId = 0;
      }
    });
  }
}
