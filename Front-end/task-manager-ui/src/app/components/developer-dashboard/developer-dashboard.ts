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
  searchTerm = '';
  statusFilter = '';
  openedTaskId: number | null = null;
  taskComments: any[] = [];
  newComment = '';
  isLoadingComments = false;
  isAddingComment = false;
  commentMessage = '';

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

  countOverdue() {
    const today = this.todayIso();
    return this.subTasks.filter((subTask) =>
      subTask.dueDate && subTask.dueDate < today && subTask.status !== 'DONE'
    ).length;
  }

  countDueToday() {
    const today = this.todayIso();
    return this.subTasks.filter((subTask) =>
      subTask.dueDate === today && subTask.status !== 'DONE'
    ).length;
  }

  completionRate() {
    if (this.subTasks.length === 0) return 0;
    return Math.round((this.countByStatus('DONE') / this.subTasks.length) * 100);
  }

  countByPriority(priority: string) {
    return this.subTasks.filter((subTask) => (subTask.priority || 'MEDIUM') === priority).length;
  }

  priorityPercent(priority: string) {
    if (this.subTasks.length === 0) return 0;
    return Math.round((this.countByPriority(priority) / this.subTasks.length) * 100);
  }

  get filteredSubTasks() {
    const search = this.searchTerm.trim().toLowerCase();

    return this.subTasks.filter((subTask) => {
      const matchesSearch =
        !search ||
        subTask.title?.toLowerCase().includes(search) ||
        String(subTask.taskId).includes(search);
      const matchesStatus = !this.statusFilter || subTask.status === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  private todayIso() {
    const now = new Date();
    const offsetDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return offsetDate.toISOString().slice(0, 10);
  }

  openComments(taskId: number) {
    this.openedTaskId = taskId;
    this.newComment = '';
    this.commentMessage = '';
    this.loadComments(taskId);
  }

  closeComments() {
    this.openedTaskId = null;
    this.taskComments = [];
    this.newComment = '';
    this.commentMessage = '';
  }

  loadComments(taskId: number) {
    this.isLoadingComments = true;
    this.api.getTaskComments(taskId).subscribe({
      next: (comments: any[]) => {
        this.taskComments = comments;
        this.isLoadingComments = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading comments:', err);
        this.taskComments = [];
        this.commentMessage = 'Could not load comments.';
        this.isLoadingComments = false;
        this.cdr.detectChanges();
      }
    });
  }

  addComment() {
    const text = this.newComment.trim();
    if (!this.openedTaskId || !text || this.isAddingComment) return;

    this.isAddingComment = true;
    this.commentMessage = '';
    this.cdr.detectChanges();

    this.api.addTaskComment(this.openedTaskId, text).subscribe({
      next: (res: any) => {
        this.commentMessage = res.message;
        if (res.success && this.openedTaskId) {
          this.newComment = '';
          this.loadComments(this.openedTaskId);
        }
        this.isAddingComment = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error adding comment:', err);
        this.commentMessage = err?.error?.message || 'Could not add comment.';
        this.isAddingComment = false;
        this.cdr.detectChanges();
      }
    });
  }
}
