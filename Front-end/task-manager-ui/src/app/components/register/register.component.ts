import { ChangeDetectorRef, Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {

  user = {
    name: '',
    email: '',
    password: '',
    role: ''
  };
  isSubmitting = false;
  message = '';

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  createUser() {
    if (!this.user.name.trim() || !this.user.email.trim() || !this.user.password || !this.user.role) {
      this.message = 'Fill all user details before creating user.';
      return;
    }

    this.isSubmitting = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.createUser(this.user)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (res: any) => {
          this.message = res.message;
          if (res.success) {
            this.user = {
              name: '',
              email: '',
              password: '',
              role: ''
            };
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error creating user:', err);
          this.message = 'Error creating user';
          this.cdr.detectChanges();
        }
      });
  }
}
