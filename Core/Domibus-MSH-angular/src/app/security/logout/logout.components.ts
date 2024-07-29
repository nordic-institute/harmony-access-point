import {Component} from "@angular/core";
import { Router } from "@angular/router";

@Component({
  templateUrl: 'logout.component.html',
  styleUrls: ['./logout.component.css']
})

export class LogoutAuthExtProviderComponent {

  constructor(private router: Router) {
  }

  login_again(): void {
    this.router.navigate(['/login']); // when external auth provider is used, this will redirect the user to / and then to the external auth provider url
  }

}
