import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-tl-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tl-dashboard.html',
  styleUrl: './tl-dashboard.css'
})
export class TlDashboardComponent implements OnInit {

  tlId = 2; // Hardcoded for now. Later this should come from login/session.
  tasks: any[] = [];
  developers: any[] = [];
  selectedTask: any = null;
  isSubmitting = false;
  message = '';

  subTask = {
    taskId: 0,
    title: '',
    assignedTo: ''
  };

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadTasks();
    this.loadDevelopers();
  }

  loadTasks() {
    this.api.getTasksForTL(this.tlId).subscribe((res: any) => {
      this.tasks = res;
    });
  }

  loadDevelopers() {
    this.api.getUsersByRole('DEVELOPER').subscribe((res: any) => {
      this.developers = res;
    });
  }

  selectTask(task: any) {
    this.selectedTask = task;
    this.subTask.taskId = task.id;
    this.message = '';
  }

  canCreateSubTask() {
    return Boolean(
      this.subTask.taskId &&
      this.subTask.title.trim() &&
      this.subTask.assignedTo &&
      !this.isSubmitting
    );
  }

  createSubTask() {
    if (!this.canCreateSubTask()) {
      this.message = 'Select a task, enter a subtask title, and choose a developer.';
      return;
    }

    this.isSubmitting = true;
    this.message = '';

    this.api.createSubTask(this.subTask).subscribe({
      next: (res: any) => {
        this.message = res.message;
        this.resetSubTaskForm();
      },
      error: (err) => {
        console.error('ERROR:', err);
        this.message = 'Error creating subtask';
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  private resetSubTaskForm() {
    this.selectedTask = null;
    this.subTask = {
      taskId: 0,
      title: '',
      assignedTo: ''
    };
  }
}
