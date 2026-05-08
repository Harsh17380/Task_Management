import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-developer-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './developer-dashboard.html',
  styleUrl: './developer-dashboard.css'
})
export class DeveloperDashboardComponent implements OnInit {

  @Input() currentUser: any = null;

  selectedDeveloperId = '';
  developers: any[] = [];
  subTasks: any[] = [];
  message = '';
  updatingSubTaskIds = new Set<number>();

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.currentUser?.role === 'DEVELOPER') {
      this.developers = [this.currentUser];
      this.selectedDeveloperId = String(this.currentUser.id);
      this.loadSubTasks();
    } else {
      this.loadDevelopers();
    }
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

  updateStatus(subTask: any, status: string) {
    if (subTask.status === status || this.updatingSubTaskIds.has(subTask.id)) {
      return;
    }

    const previousStatus = subTask.status;
    this.updatingSubTaskIds.add(subTask.id);
    subTask.status = status;
    this.message = '';
    this.cdr.detectChanges();

    this.api.updateSubTaskStatus(subTask.id, status)
      .pipe(finalize(() => {
        this.updatingSubTaskIds.delete(subTask.id);
        this.cdr.detectChanges();
      }))
      .subscribe({
      next: (res: any) => {
        this.message = res.message;
        if (res.success) {
          this.loadSubTasks();
        }
      },
      error: (err) => {
        console.error('Error updating subtask:', err);
        subTask.status = previousStatus;
        this.message = 'Could not update subtask status.';
      }
    });
  }

  isUpdating(subTaskId: number) {
    return this.updatingSubTaskIds.has(subTaskId);
  }

  countByStatus(status: string) {
    return this.subTasks.filter((subTask) => subTask.status === status).length;
  }
}
