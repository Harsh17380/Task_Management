import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  @Output() loggedIn = new EventEmitter<any>();

  credentials = {
    email: '',
    password: ''
  };
  isSubmitting = false;
  message = '';

  constructor(private api: ApiService) {}

  login() {
    if (!this.credentials.email.trim() || !this.credentials.password) {
      this.message = 'Enter email and password.';
      return;
    }

    this.isSubmitting = true;
    this.message = '';

    this.api.login(this.credentials)
      .pipe(finalize(() => {
        this.isSubmitting = false;
      }))
      .subscribe({
        next: (res: any) => {
          this.message = res.message;

          if (res.success && res.data) {
            this.loggedIn.emit(res.data);
          }
        },
        error: (err) => {
          console.error('Login error:', err);
          this.message = 'Login failed. Please check backend server.';
        }
      });
  }
}
