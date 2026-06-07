import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DeveloperDashboardComponent } from './components/developer-dashboard/developer-dashboard';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register.component';
import { SupervisorDashboardComponent } from './components/supervisor-dashboard/supervisor-dashboard';
import { TaskCreateComponent } from './components/task-create/task-create';
import { TlDashboardComponent } from './components/tl-dashboard/tl-dashboard';
import { AuthService } from './services/auth.service';
import { ChangePasswordComponent } from './components/change-password/change-password';
import { UserManagementComponent } from './components/user-management/user-management';
import { ApiService } from './services/api.service';

type ActiveView =
  | 'register'
  | 'task-create'
  | 'supervisor-dashboard'
  | 'tl-dashboard'
  | 'developer-dashboard'
  | 'change-password'
  | 'user-management';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    LoginComponent,
    RegisterComponent,
    SupervisorDashboardComponent,
    TaskCreateComponent,
    TlDashboardComponent,
    DeveloperDashboardComponent,
    ChangePasswordComponent,
    UserManagementComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  currentUser: any = null;
  activeView: ActiveView = 'tl-dashboard';

  constructor(
    private authService: AuthService,
    private api: ApiService,
  ) {
    // Restore session on app load — validates token expiry too
    if (authService.isLoggedIn()) {
      this.currentUser = authService.getUser();
      this.activeView = this.getDefaultView(this.currentUser?.role);
    }
  }

  onLoggedIn(user: any) {
    this.api.clearUserCache();
    this.currentUser = user;
    this.activeView = this.getDefaultView(user.role);
  }

  logout() {
    this.authService.logout();
    this.api.clearUserCache();
    this.currentUser = null;
    this.activeView = 'tl-dashboard';
  }

  setActiveView(view: ActiveView) {
    this.activeView = view;
  }

  canAccess(view: ActiveView): boolean {
    if (!this.currentUser) return false;

    const role = this.currentUser.role;

    if (view === 'user-management') {
      return this.isAdminUser();
    }

    if (role === 'SUPER_ADMIN' || role === 'COMPANY_ADMIN') {
      return ['register', 'user-management', 'change-password'].includes(view);
    }

    if (role === 'SUPERVISOR') {
      return [
        'task-create',
        'supervisor-dashboard',
        'change-password',
      ].includes(view);
    }

    if (role === 'TL') {
      return ['tl-dashboard', 'change-password'].includes(view);
    }

    if (role === 'DEVELOPER') {
      return ['developer-dashboard', 'change-password'].includes(view);
    }

    if (role === 'MANAGER') {
      return ['change-password'].includes(view);
    }

    return false;
  }

  private isAdminUser(): boolean {
    return ['SUPER_ADMIN', 'COMPANY_ADMIN'].includes(this.currentUser?.role);
  }

  private getDefaultView(role?: string): ActiveView {
    if (role === 'SUPER_ADMIN' || role === 'COMPANY_ADMIN') return 'user-management';
    if (role === 'SUPERVISOR') return 'supervisor-dashboard';
    if (role === 'DEVELOPER') return 'developer-dashboard';
    if (role === 'MANAGER') return 'change-password';
    return 'tl-dashboard';
  }
}
