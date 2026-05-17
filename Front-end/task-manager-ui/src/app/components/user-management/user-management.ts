import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css'
})
export class UserManagementComponent implements OnInit {

  @Input() currentUser: any = null;

  users: any[] = [];
  isLoading = false;
  message = '';
  messageType: 'success' | 'error' | '' = '';
  userPendingDelete: any = null;
  deletingUserIds = new Set<number>();

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    this.message = '';
    this.messageType = '';
    this.cdr.detectChanges();

    this.api.getAllUsers().subscribe({
      next: (res: any) => {
        this.users = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.message = 'Could not load users. Please check backend server.';
        this.messageType = 'error';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Returns true if the logged-in user can delete the target user
  canDelete(targetUser: any): boolean {
    if (!this.currentUser) return false;

    // Cannot delete yourself
    if (this.currentUser.id === targetUser.id) return false;

    if (!targetUser.status) return false;

    return this.currentUser.email?.toLowerCase() === 'admin@corequeue.com';
  }

  deleteUser(user: any) {
    if (!this.canDelete(user)) return;

    this.userPendingDelete = user;
    this.message = '';
    this.messageType = '';
    this.cdr.detectChanges();
  }

  cancelDelete() {
    this.userPendingDelete = null;
    this.cdr.detectChanges();
  }

  confirmDelete() {
    const user = this.userPendingDelete;
    if (!user || !this.canDelete(user)) return;

    this.deletingUserIds.add(user.id);
    this.message = '';
    this.messageType = '';
    this.cdr.detectChanges();

    this.api.deleteUser(user.id)
      .pipe(finalize(() => {
        this.deletingUserIds.delete(user.id);
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (res: any) => {
          this.message = res.message;
          this.messageType = res.success ? 'success' : 'error';
          if (res.success) {
            this.users = this.users.filter(u => u.id !== user.id);
            this.userPendingDelete = null;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          this.message = 'Error deleting user.';
          this.messageType = 'error';
          this.cdr.detectChanges();
        }
      });
  }

  isDeleting(userId: number): boolean {
    return this.deletingUserIds.has(userId);
  }

  countByRole(role: string): number {
    return this.users.filter(u => u.role === role).length;
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'SUPERVISOR': return 'badge-supervisor';
      case 'TL': return 'badge-tl';
      case 'DEVELOPER': return 'badge-developer';
      default: return '';
    }
  }
}
