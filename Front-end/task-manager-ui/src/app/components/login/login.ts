import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

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
  showPassword = false;

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}
  login() {
    if (!this.credentials.email.trim() || !this.credentials.password) {
      this.message = 'Enter email and password.';
      this.cdr.detectChanges();
      return;
    }

    this.isSubmitting = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.login(this.credentials)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (res: any) => {
          this.message = res.message;

          if (res.success && res.data) {
            const userData = res.data;

            // ✅ Save JWT token separately via AuthService
            if (userData.token) {
              this.authService.saveToken(userData.token);
            }

            // ✅ Save user profile (without token)
            this.authService.saveUser(userData);

            // Emit user data (without token) to parent
            const { token, ...userWithoutToken } = userData;
            this.loggedIn.emit(userWithoutToken);
         }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Login error:', err);
          this.message = 'Login failed. Please check backend server.';
          this.cdr.detectChanges();
        }
      });
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }
}
