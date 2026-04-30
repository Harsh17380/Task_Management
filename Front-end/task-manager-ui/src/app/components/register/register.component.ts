import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-register',
   standalone: true,
  imports: [FormsModule],
  templateUrl: './register.html'
})
export class RegisterComponent {

  user = {
    name: '',
    email: '',
    password: '',
    role: ''
  };

  constructor(private api: ApiService) {}
  createUser() {
  this.api.createUser(this.user).subscribe((res: any) => {
    alert(res.message);
  });
}
}