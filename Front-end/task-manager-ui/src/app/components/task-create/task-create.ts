import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-task-create',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './task-create.html',
  styleUrl: './task-create.css'
})
export class TaskCreateComponent implements OnInit {

  @Input() currentUser: any = null;

  task = {
    title: '',
    description: '',
    assignedTo: '',
    createdBy: 0
  };

  tls: any[] = [];
  isSubmitting = false;
  message = '';

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.task.createdBy = this.currentUser?.id || 0;

    this.api.getUsersByRole('TL').subscribe({
      next: (res: any) => {
        this.tls = res;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading TL users:', err);
        this.message = 'Could not load team leads. Please check backend server.';
        this.cdr.detectChanges();
      }
    });
  }

  createTask() {
    if (!this.task.title.trim() || !this.task.assignedTo) {
      this.message = 'Enter task title and select a team lead.';
      return;
    }

    this.isSubmitting = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.createTask(this.task)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (res: any) => {
          this.message = res.message;
          if (res.success) {
          this.task = {
            title: '',
            description: '',
            assignedTo: '',
              createdBy: this.currentUser?.id || 0
            };
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error creating task:', err);
          this.message = 'Error creating task';
          this.cdr.detectChanges();
        }
      });
  }
}
