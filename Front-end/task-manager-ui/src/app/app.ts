import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DeveloperDashboardComponent } from './components/developer-dashboard/developer-dashboard';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register.component';
import { SupervisorDashboardComponent } from './components/supervisor-dashboard/supervisor-dashboard';
import { TaskCreateComponent } from './components/task-create/task-create';
import { TlDashboardComponent } from './components/tl-dashboard/tl-dashboard';
import { AuthService } from './services/auth.service';

type ActiveView = 'register' | 'task-create' | 'supervisor-dashboard' | 'tl-dashboard' | 'developer-dashboard';

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
    DeveloperDashboardComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  currentUser: any = null;
  activeView: ActiveView = 'tl-dashboard';

  constructor(private authService: AuthService) {
    // ✅ Restore session on app load — validates token expiry too
    if (authService.isLoggedIn()) {
      this.currentUser = authService.getUser();
      this.activeView = this.getDefaultView(this.currentUser?.role);
    }
  }

  onLoggedIn(user: any) {
    this.currentUser = user;
    this.activeView = this.getDefaultView(user.role);
  }

  logout() {
    // ✅ Clears both token and user profile
    this.authService.logout();
    this.currentUser = null;
    this.activeView = 'tl-dashboard';
  }

  setActiveView(view: ActiveView) {
    this.activeView = view;
  }

  canAccess(view: ActiveView) {
    if (!this.currentUser) return false;

    if (this.currentUser.role === 'SUPERVISOR') {
      return view === 'register' || view === 'task-create' || view === 'supervisor-dashboard';
    }
    if (this.currentUser.role === 'TL') {
      return view === 'tl-dashboard';
    }
    if (this.currentUser.role === 'DEVELOPER') {
      return view === 'developer-dashboard';
    }
    return false;
  }

  private getDefaultView(role?: string): ActiveView {
    if (role === 'SUPERVISOR') return 'supervisor-dashboard';
    if (role === 'DEVELOPER') return 'developer-dashboard';
    return 'tl-dashboard';
  }
}
