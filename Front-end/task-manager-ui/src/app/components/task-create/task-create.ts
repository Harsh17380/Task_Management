import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-task-create',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './task-create.html'
})
export class TaskCreateComponent implements OnInit {

  task = {
    title: '',
    description: '',
    assignedTo: '',
    createdBy: 1   // for now, hardcode Supervisor ID
  };

  tls: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.api.getUsersByRole('TL').subscribe((res: any) => {
      this.tls = res;
    });
  }

  createTask() {
    console.log("TASK DATA:", this.task);
    this.api.createTask(this.task).subscribe((res: any) => {
      alert(res.message);
    });
  }
}