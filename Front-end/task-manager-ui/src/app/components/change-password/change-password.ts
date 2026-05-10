import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePasswordComponent {

  oldPassword = '';
  newPassword = '';

  message = '';
  error = '';
  loading = false;

  constructor(
    private api: ApiService,
    private authService: AuthService
  ) {}

  changePassword() {

    this.loading = true;
    this.message = '';
    this.error = '';

    const body = {
      oldPassword: this.oldPassword,
      newPassword: this.newPassword
    };

    this.api.changePassword(body)
      .subscribe({

        next: (res: any) => {

          this.loading = false;

          if (res.success) {

            alert('Password changed successfully. Please login again.');

            this.authService.logout();

            window.location.reload();

          } else {
            this.error = res.message;
          }
        },

        error: (err) => {

          console.error(err);

          this.loading = false;
          this.error = 'Password change failed';
        }
      });
  }
}