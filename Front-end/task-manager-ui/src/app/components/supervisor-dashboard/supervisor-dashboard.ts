import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { CommentBadgeService } from '../../services/comment-badge.service';

@Component({
  selector: 'app-supervisor-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './supervisor-dashboard.html',
  styleUrl: './supervisor-dashboard.css'
})
export class SupervisorDashboardComponent implements OnInit, OnChanges {

  @Input() currentUser: any = null;
  @ViewChild('commentList') commentList?: ElementRef<HTMLElement>;

  tasks: any[] = [];
  message = '';
  isLoading = false;
  searchTerm = '';
  statusFilter = '';
  priorityFilter = '';
  openedTask: any = null;
  detailMode: 'details' | 'comments' = 'details';
  taskComments: any[] = [];
  newComment = '';
  isLoadingComments = false;
  isAddingComment = false;
  commentMessage = '';
  private initializedForUserId: number | null = null;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef,
    public commentBadge: CommentBadgeService,
  ) {}

  ngOnInit() {
    this.initializeDashboard();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['currentUser']) {
      this.initializeDashboard();
    }
  }

  private initializeDashboard() {
    if (!this.currentUser?.id || this.initializedForUserId === this.currentUser.id) {
      return;
    }

    this.initializedForUserId = this.currentUser.id;
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
        // Load comment badges for all tasks
        const taskIds = res.map((t: any) => t.id as number);
        this.commentBadge.loadBadges(taskIds);
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

  countOverdue() {
    const today = this.todayIso();
    return this.tasks.filter((task) =>
      task.dueDate && task.dueDate < today && task.status !== 'COMPLETED'
    ).length;
  }

  countDueToday() {
    const today = this.todayIso();
    return this.tasks.filter((task) =>
      task.dueDate === today && task.status !== 'COMPLETED'
    ).length;
  }

  completionRate() {
    if (this.tasks.length === 0) return 0;
    return Math.round((this.countByStatus('COMPLETED') / this.tasks.length) * 100);
  }

  countByPriority(priority: string) {
    return this.tasks.filter((task) => (task.priority || 'MEDIUM') === priority).length;
  }

  priorityPercent(priority: string) {
    if (this.tasks.length === 0) return 0;
    return Math.round((this.countByPriority(priority) / this.tasks.length) * 100);
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

  get priorityLevels() {
    return ['URGENT', 'HIGH', 'MEDIUM', 'LOW'];
  }

  private todayIso() {
    const now = new Date();
    const offsetDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return offsetDate.toISOString().slice(0, 10);
  }

  openTask(task: any, mode: 'details' | 'comments' = 'details') {
    this.openedTask = task;
    this.detailMode = mode;
    this.newComment = '';
    this.commentMessage = '';
    if (mode === 'comments') {
      this.commentBadge.markSeen(task.id);
      this.loadComments(task.id);
    } else {
      this.taskComments = [];
    }
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
        this.scrollCommentsToBottom();
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

  submitCommentOnEnter(event: Event) {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.shiftKey) return;
    keyboardEvent.preventDefault();
    this.addComment();
  }

  private scrollCommentsToBottom() {
    setTimeout(() => {
      const element = this.commentList?.nativeElement;
      if (element) {
        element.scrollTop = element.scrollHeight;
      }
    });
  }
}
