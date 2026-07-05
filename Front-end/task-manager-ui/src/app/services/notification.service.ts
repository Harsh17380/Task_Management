import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, interval, Subscription } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface AppNotification {
  id: number;
  message: string;
  type: string;
  referenceId: number | null;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private _notifications = new BehaviorSubject<AppNotification[]>([]);
  private _unreadCount = new BehaviorSubject<number>(0);
  private _optimisticReadIds = new Set<number>();
  private pollSub: Subscription | null = null;

  notifications$ = this._notifications.asObservable();
  unreadCount$ = this._unreadCount.asObservable();

  constructor(private api: ApiService) {}

  /** Call once after login to start polling. */
  startPolling() {
    this.stopPolling();
    // Track IDs we've optimistically marked read but server hasn't confirmed yet
    this._optimisticReadIds.clear();

    this.pollSub = interval(30_000)
      .pipe(startWith(0), switchMap(() => this.api.getNotifications()))
      .subscribe({
        next: (list: AppNotification[]) => {
          // Apply any pending optimistic reads on top of fresh server data
          const merged = list.map((n) =>
            this._optimisticReadIds.has(n.id) ? { ...n, isRead: true } : n,
          );
          this._notifications.next(merged);
          this._unreadCount.next(merged.filter((n) => !n.isRead).length);

          // Once server confirms an item is read, remove it from our optimistic set
          list.filter((n) => n.isRead).forEach((n) => this._optimisticReadIds.delete(n.id));
        },
        error: () => {},
      });
  }

  /** Call on logout to stop polling. */
  stopPolling() {
    this.pollSub?.unsubscribe();
    this.pollSub = null;
    this._notifications.next([]);
    this._unreadCount.next(0);
    this._optimisticReadIds.clear();
  }

  markAsRead(notificationId: number) {
    const current = this._notifications.value;
    const alreadyRead = current.find((n) => n.id === notificationId)?.isRead;
    if (alreadyRead) return;

    // Register in optimistic set so polls don't overwrite it
    this._optimisticReadIds.add(notificationId);

    // Update local state immediately
    const updated = current.map((n) =>
      n.id === notificationId ? { ...n, isRead: true } : n,
    );
    this._notifications.next(updated);
    this._unreadCount.next(updated.filter((n) => !n.isRead).length);

    // Persist to server
    this.api.markNotificationRead(notificationId).subscribe({
      next: () => {
        // Server confirmed — remove from optimistic set
        this._optimisticReadIds.delete(notificationId);
      },
      error: () => {
        // Revert optimistic update on failure
        this._optimisticReadIds.delete(notificationId);
        const reverted = this._notifications.value.map((n) =>
          n.id === notificationId ? { ...n, isRead: false } : n,
        );
        this._notifications.next(reverted);
        this._unreadCount.next(reverted.filter((n) => !n.isRead).length);
      },
    });
  }

  markAllAsRead() {
    const unreadIds = this._notifications.value
      .filter((n) => !n.isRead)
      .map((n) => n.id);

    // Register all in optimistic set
    unreadIds.forEach((id) => this._optimisticReadIds.add(id));

    // Update local state immediately
    const updated = this._notifications.value.map((n) => ({ ...n, isRead: true }));
    this._notifications.next(updated);
    this._unreadCount.next(0);

    this.api.markAllNotificationsRead().subscribe({
      next: () => {
        // Server confirmed — clear all optimistic IDs
        unreadIds.forEach((id) => this._optimisticReadIds.delete(id));
      },
      error: () => {
        // Revert on failure
        unreadIds.forEach((id) => this._optimisticReadIds.delete(id));
        this.refresh();
      },
    });
  }

  /** Returns the current snapshot of notifications synchronously. */
  getSnapshot(): AppNotification[] {
    return this._notifications.value;
  }

  /** Force an immediate refresh (e.g. after adding a comment). */
  refresh() {
    this.api.getNotifications().subscribe({
      next: (list: AppNotification[]) => {
        const merged = list.map((n) =>
          this._optimisticReadIds.has(n.id) ? { ...n, isRead: true } : n,
        );
        this._notifications.next(merged);
        this._unreadCount.next(merged.filter((n) => !n.isRead).length);
        list.filter((n) => n.isRead).forEach((n) => this._optimisticReadIds.delete(n.id));
      },
      error: () => {},
    });
  }

  ngOnDestroy() {
    this.stopPolling();
  }
}
