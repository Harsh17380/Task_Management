import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  HostListener,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { AppNotification, NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: AppNotification[] = [];
  unreadCount = 0;
  isOpen = false;
  typeFilter = '';
  private subs: Subscription[] = [];

  constructor(
    private notifService: NotificationService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    this.subs.push(
      this.notifService.notifications$.subscribe((list) => {
        this.notifications = list;
        this.cdr.markForCheck();
      }),
      this.notifService.unreadCount$.subscribe((count) => {
        this.unreadCount = count;
        this.cdr.markForCheck();
      }),
    );
  }

  togglePanel() {
    this.isOpen = !this.isOpen;
  }

  markRead(n: AppNotification, event: Event) {
    event.stopPropagation();
    if (!n.isRead) {
      this.notifService.markAsRead(n.id);
    }
  }

  markAllRead() {
    this.notifService.markAllAsRead();
  }

  closePanel() {
    this.isOpen = false;
  }

  get filteredNotifications(): AppNotification[] {
    if (!this.typeFilter) {
      return this.notifications;
    }
    return this.notifications.filter((n) => n.type === this.typeFilter);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.notification-wrapper')) {
      this.isOpen = false;
      this.cdr.markForCheck();
    }
  }

  typeLabel(type: string): string {
    switch (type) {
      case 'TASK_ASSIGNED':
        return 'TASK';
      case 'SUBTASK_ASSIGNED':
        return 'SUB';
      case 'NEW_COMMENT':
        return 'COMMENT';
      case 'SUBTASK_STATUS':
      case 'TASK_STATUS':
        return 'STATUS';
      case 'DUE_SOON':
        return 'DUE';
      default:
        return 'INFO';
    }
  }

  timeAgo(dateStr: string): string {
    const timestamp = new Date(dateStr).getTime();
    if (Number.isNaN(timestamp)) {
      return '';
    }

    const diff = Date.now() - timestamp;
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  }

  ngOnDestroy() {
    this.subs.forEach((s) => s.unsubscribe());
  }
}
