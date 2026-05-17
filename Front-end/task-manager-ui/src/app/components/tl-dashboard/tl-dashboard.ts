import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-tl-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tl-dashboard.html',
  styleUrl: './tl-dashboard.css'
})
export class TlDashboardComponent implements OnInit {

  @Input() currentUser: any = null;

  selectedTlId = '';
  teamLeads: any[] = [];
  tasks: any[] = [];
  developers: any[] = [];
  selectedTask: any = null;
  isSubmitting = false;
  message = '';
  searchTerm = '';
  statusFilter = '';
  priorityFilter = '';

  subTask = {
    taskId: 0,
    title: '',
    assignedTo: '',
    tlId: 0
  };

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    if (this.currentUser?.role === 'TL') {
      this.teamLeads = [this.currentUser];
      this.selectedTlId = String(this.currentUser.id);
      this.subTask.tlId = this.currentUser.id;
      this.loadTasks();
    } else {
      this.loadTeamLeads();
    }

    this.loadDevelopers();
  }

  loadTeamLeads() {
    this.api.getUsersByRole('TL').subscribe({
      next: (res: any) => {
        this.teamLeads = res;

        if (this.teamLeads.length > 0) {
          this.selectedTlId = String(this.teamLeads[0].id);
          this.subTask.tlId = Number(this.selectedTlId);
          this.loadTasks();
        }
      },
      error: (err) => {
        console.error('Error loading team leads:', err);
        this.message = 'Could not load team leads. Please check backend server.';
      }
    });
  }

  loadTasks() {
    if (!this.selectedTlId) {
      this.tasks = [];
      return;
    }

    this.api.getTasksForTL(Number(this.selectedTlId)).subscribe({
      next: (res: any) => {
        this.tasks = res;
        this.resetSubTaskForm();
      },
      error: (err) => {
        console.error('Error loading tasks:', err);
        this.tasks = [];
        this.message = 'Could not load tasks for selected team lead.';
      }
    });
  }

  loadDevelopers() {
    this.api.getUsersByRole('DEVELOPER').subscribe({
      next: (res: any) => {
        this.developers = res;
      },
      error: (err) => {
        console.error('Error loading developers:', err);
        this.developers = [];
        this.message = 'Could not load developers. Please check backend server.';
      }
    });
  }

  selectTask(task: any) {
    this.selectedTask = task;
    this.subTask.taskId = task.id;
    this.message = '';
  }

  onTeamLeadChange() {
    this.message = '';
    this.subTask.tlId = Number(this.selectedTlId);
    this.loadTasks();
  }

  canCreateSubTask() {
    return Boolean(
      this.subTask.taskId &&
      this.subTask.tlId &&
      this.subTask.title.trim() &&
      this.subTask.assignedTo &&
      !this.isSubmitting
    );
  }

  countByStatus(status: string) {
    return this.tasks.filter((task) => task.status === status).length;
  }

  get filteredTasks() {
    const search = this.searchTerm.trim().toLowerCase();

    return this.tasks.filter((task) => {
      const matchesSearch =
        !search ||
        task.title?.toLowerCase().includes(search) ||
        task.description?.toLowerCase().includes(search);
      const matchesStatus = !this.statusFilter || task.status === this.statusFilter;
      const matchesPriority = !this.priorityFilter || task.priority === this.priorityFilter;

      return matchesSearch && matchesStatus && matchesPriority;
    });
  }

  getPriorityClass(priority: string) {
    return `priority-${(priority || 'MEDIUM').toLowerCase()}`;
  }

  createSubTask() {
    if (!this.canCreateSubTask()) {
      this.message = 'Select a task, enter a subtask title, and choose a developer.';
      return;
    }

    this.isSubmitting = true;
    this.message = '';
    this.cdr.detectChanges();

    this.api.createSubTask(this.subTask)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
      next: (res: any) => {
        this.message = res.message;
        if (res.success) {
          this.resetSubTaskForm();
        }
      },
      error: (err) => {
        console.error('ERROR:', err);
        this.message = 'Error creating subtask';
      }
    });
  }

  private resetSubTaskForm() {
    this.selectedTask = null;
    this.subTask = {
      taskId: 0,
      title: '',
      assignedTo: '',
      tlId: Number(this.selectedTlId)
    };
  }
}
