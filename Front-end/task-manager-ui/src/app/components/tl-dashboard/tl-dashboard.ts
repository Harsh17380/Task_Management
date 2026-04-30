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

  selectedTlId = '';
  teamLeads: any[] = [];
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
    this.loadTeamLeads();
    this.loadDevelopers();
  }

  loadTeamLeads() {
    this.api.getUsersByRole('TL').subscribe({
      next: (res: any) => {
        this.teamLeads = res;

        if (this.teamLeads.length > 0) {
          this.selectedTlId = String(this.teamLeads[0].id);
          this.loadTasks();
        }
      },
      error: (err) => {
        console.error('Error loading team leads:', err);
        this.message = 'Could not load team leads. Please check backend server.';
      }
    });
  }

  loadTasks() {
    if (!this.selectedTlId) {
      this.tasks = [];
      return;
    }

    this.api.getTasksForTL(Number(this.selectedTlId)).subscribe({
      next: (res: any) => {
        this.tasks = res;
        this.resetSubTaskForm();
      },
      error: (err) => {
        console.error('Error loading tasks:', err);
        this.tasks = [];
        this.message = 'Could not load tasks for selected team lead.';
      }
    });
  }

  loadDevelopers() {
    this.api.getUsersByRole('DEVELOPER').subscribe({
      next: (res: any) => {
        this.developers = res;
      },
      error: (err) => {
        console.error('Error loading developers:', err);
        this.developers = [];
        this.message = 'Could not load developers. Please check backend server.';
      }
    });
  }

  selectTask(task: any) {
    this.selectedTask = task;
    this.subTask.taskId = task.id;
    this.message = '';
  }

  onTeamLeadChange() {
    this.message = '';
    this.loadTasks();
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
