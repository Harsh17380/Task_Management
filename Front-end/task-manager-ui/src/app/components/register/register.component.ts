import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent implements OnInit {
  @Input() currentUser: any = null;

  user = {
    name: '',
    email: '',
    password: '',
    role: '',
    companyName: '',
  };
  isSubmitting = false;
  message = '';
  messageType: 'success' | 'error' | '' = '';
  users: any[] = [];

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.resetForm();
  }

  get isSuperAdmin(): boolean {
    return this.currentUser?.role === 'SUPER_ADMIN';
  }

  createUser() {
    if (
      !this.user.name.trim() ||
      !this.user.email.trim() ||
      !this.user.password ||
      !this.user.role ||
      (this.isSuperAdmin && !this.user.companyName.trim())
    ) {
      this.message = 'Fill all user details before creating user.';
      this.messageType = 'error';
      this.cdr.detectChanges();
      return;
    }

    this.isSubmitting = true;
    this.message = '';
    this.messageType = '';
    this.cdr.detectChanges();

    this.api
      .createUser(this.user)
      .pipe(
        finalize(() => {
          this.isSubmitting = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: (res: any) => {
          this.message = res.message;
          this.messageType = res.success ? 'success' : 'error';
          if (res.success) {
            this.resetForm();
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error creating user:', err);
          this.message = 'Error creating user';
          this.messageType = 'error';
          this.cdr.detectChanges();
        },
      });
  }

  private resetForm() {
    this.user = {
      name: '',
      email: '',
      password: '',
      role: this.isSuperAdmin ? 'COMPANY_ADMIN' : '',
      companyName: '',
    };
  }
  /*loadUsers() {
    this.api.getAllUsers().subscribe({
      next: (res: any) => {
        this.users = res;
        console.log(this.users);
      },

      error: (err) => {
        console.error(err);
      },
    });
  }
  deleteUser(userId: number) {
    const confirmed = confirm('Are you sure you want to delete this user?');

    if (!confirmed) return;

    this.api.deleteUser(userId).subscribe({
      next: (res: any) => {
        alert(res.message);

        this.loadUsers();
      },

      error: (err) => {
        console.error(err);

        alert('Delete failed');
      },
    });
  }*/
}
