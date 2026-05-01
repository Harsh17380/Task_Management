import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DeveloperDashboardComponent } from './components/developer-dashboard/developer-dashboard';
import { RegisterComponent } from './components/register/register.component';
import { TaskCreateComponent } from './components/task-create/task-create';
import { TlDashboardComponent } from './components/tl-dashboard/tl-dashboard';

type ActiveView = 'register' | 'task-create' | 'tl-dashboard' | 'developer-dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RegisterComponent,
    TaskCreateComponent,
    TlDashboardComponent,
    DeveloperDashboardComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  activeView: ActiveView = 'tl-dashboard';

  setActiveView(view: ActiveView) {
    this.activeView = view;
  }
}
