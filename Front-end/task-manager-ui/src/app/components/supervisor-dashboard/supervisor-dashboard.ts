import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';


@Component({
  selector: 'app-supervisor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './supervisor-dashboard.html',
  styleUrl: './supervisor-dashboard.css'
})
export class SupervisorDashboardComponent implements OnInit {

  @Input() currentUser: any = null;

  tasks: any[] = [];
  message = '';
  isLoading = false;
  searchTerm = '';
  statusFilter = '';
  priorityFilter = '';
  openedTask: any = null;
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
    this.loadTasks();
  }

  loadTasks() {
    if (!this.currentUser?.id) {
      this.tasks = [];
      return;
    }

    this.isLoading = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.getTasksForSupervisor(this.currentUser.id).subscribe({
      next: (res: any) => {
        this.tasks = res;
        this.message = '';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading supervisor tasks:', err);
        this.tasks = [];
        this.message = 'Could not load task tracking data.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  countByStatus(status: string) {
    return this.tasks.filter((task) => task.status === status).length;
  }

  get filteredTasks() {
    const search = this.searchTerm.trim().toLowerCase();

    return this.tasks.filter((task) => {
      const matchesSearch =
        !search ||
        task.title?.toLowerCase().includes(search) ||
        task.description?.toLowerCase().includes(search) ||
        task.assignedToName?.toLowerCase().includes(search);
      const matchesStatus = !this.statusFilter || task.status === this.statusFilter;
      const matchesPriority = !this.priorityFilter || task.priority === this.priorityFilter;

      return matchesSearch && matchesStatus && matchesPriority;
    });
  }

  getPriorityClass(priority: string) {
    return `priority-${(priority || 'MEDIUM').toLowerCase()}`;
  }

  openTask(task: any) {
    this.openedTask = task;
    this.newComment = '';
    this.commentMessage = '';
    this.loadComments(task.id);
  }

  closeTask() {
    this.openedTask = null;
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
    if (!this.openedTask || !text || this.isAddingComment) return;

    this.isAddingComment = true;
    this.commentMessage = '';
    this.cdr.detectChanges();

    this.api.addTaskComment(this.openedTask.id, text).subscribe({
      next: (res: any) => {
        this.commentMessage = res.message;
        if (res.success) {
          this.newComment = '';
          this.loadComments(this.openedTask.id);
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
