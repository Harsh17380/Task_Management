import { Injectable } from '@angular/core';
import { NotificationService, AppNotification } from './notification.service';

/**
 * Derives "new comment" badge counts from already-loaded notifications.
 * Uses unread NEW_COMMENT notifications (referenceId = taskId).
 * No extra HTTP calls — feeds off the existing notification poll.
 */
@Injectable({ providedIn: 'root' })
export class CommentBadgeService {

  constructor(private notifService: NotificationService) {}

  /**
   * Returns the number of unread NEW_COMMENT notifications for a given taskId.
   * Safe to call from templates — reads from the exposed snapshot.
   */
  getBadge(taskId: number): number {
    return this.notifService.getSnapshot().filter(
      (n) => n.type === 'NEW_COMMENT' && n.referenceId === taskId && !n.isRead,
    ).length;
  }

  /**
   * Call when the user opens the comment panel for a task.
   * Marks all unread NEW_COMMENT notifications for that task as read.
   */
  markSeen(taskId: number) {
    const unread = this.notifService.getSnapshot().filter(
      (n) => n.type === 'NEW_COMMENT' && n.referenceId === taskId && !n.isRead,
    );
    unread.forEach((n) => this.notifService.markAsRead(n.id));
  }

  /** Called from app.ts on login — no-op here, init is handled by NotificationService */
  init(_userId: number) {}

  /** Called from app.ts on logout — no-op here, reset is handled by NotificationService */
  reset() {}

  /** Called from dashboard components after tasks load — no-op, badges come from notifications */
  loadBadges(_taskIds: number[]) {}
}
