import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';


@Component({
  selector: 'app-supervisor-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './supervisor-dashboard.html',
  styleUrl: './supervisor-dashboard.css'
})
export class SupervisorDashboardComponent implements OnInit {

  @Input() currentUser: any = null;

  tasks: any[] = [];
  message = '';
  isLoading = false;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    if (!this.currentUser?.id) {
      this.tasks = [];
      return;
    }

    this.isLoading = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.getTasksForSupervisor(this.currentUser.id).subscribe({
      next: (res: any) => {
        this.tasks = res;
        this.message = '';
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading supervisor tasks:', err);
        this.tasks = [];
        this.message = 'Could not load task tracking data.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  countByStatus(status: string) {
    return this.tasks.filter((task) => task.status === status).length;
  }
}
