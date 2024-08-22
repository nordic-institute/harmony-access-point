import {Component} from "@angular/core";

@Component({
  templateUrl: 'logout.component.html',
  styleUrls: ['./logout.component.css']
})

export class LogoutAuthExtProviderComponent {

  constructor() {
  }

  login_again(): void {
    // When external auth provider is used, we rely on the webserver serving the pages for authentication;
    // so we need to request the page from the server, and we achieve this by changing the window.location
    // (simply using the router to navigate to the login page/other page will not trigger the authentication)

    console.log(`Logging in again ...`);
    let newurl = window.location.protocol + "//" + window.location.host + window.location.pathname;
    newurl = newurl.replace(/\/logout\/?$/, ''); // replace "/logout" only at the end of the path

    console.log(`Redirecting from ${window.location.href} to ${newurl} ...`)
    window.location.href = newurl;
  }

}
