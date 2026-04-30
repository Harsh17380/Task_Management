import { Component } from '@angular/core';
/*import { RegisterComponent } from './components/register/register.component';
import { TaskCreateComponent } from './components/task-create/task-create';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RegisterComponent,TaskCreateComponent],
  template: `<app-register></app-register>`
})
export class App {}*/

import { TaskCreateComponent } from './components/task-create/task-create';
import { TlDashboardComponent } from './components/tl-dashboard/tl-dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TlDashboardComponent],
  //template: `<app-task-create></app-task-create>`
  template: `<app-tl-dashboard></app-tl-dashboard>`
})
export class App {}