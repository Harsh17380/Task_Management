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

  canDelete(targetUser: any): boolean {
    if (!this.currentUser) return false;
    if (this.currentUser.id === targetUser.id) return false;
    if (!targetUser.status) return false;

    if (this.currentUser.role === 'SUPER_ADMIN') {
      return targetUser.role !== 'SUPER_ADMIN';
    }

    return this.currentUser.role === 'COMPANY_ADMIN'
      && targetUser.companyId === this.currentUser.companyId
      && !['SUPER_ADMIN', 'COMPANY_ADMIN'].includes(targetUser.role);
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

  countByUserStatus(isActive: boolean): number {
    return this.users.filter(u => Boolean(u.status) === isActive).length;
  }

  countCompanies(): number {
    return new Set(this.users.map(u => u.companyId).filter(Boolean)).size;
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'SUPER_ADMIN': return 'badge-super-admin';
      case 'COMPANY_ADMIN': return 'badge-company-admin';
      case 'SUPERVISOR': return 'badge-supervisor';
      case 'MANAGER': return 'badge-manager';
      case 'TL': return 'badge-tl';
      case 'DEVELOPER': return 'badge-developer';
      default: return '';
    }
  }
}
