import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SecurityService} from '../security.service';
import {AlertService} from '../../common/alert/alert.service';
import {SecurityEventService} from '../security.event.service';
import {MatDialog} from '@angular/material';
import {DefaultPasswordDialogComponent} from 'app/security/default-password-dialog/default-password-dialog.component';
import {Server} from '../Server';

@Component({
  moduleId: module.id,
  templateUrl: 'login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {
  model: any = {};
  returnUrl: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private securityService: SecurityService,
              private alertService: AlertService,
              private securityEventService: SecurityEventService,
              private dialog: MatDialog) {

  }

  ngOnInit() {
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  async login() {
    try {
      await this.securityService.login(this.model.username, this.model.password);
      this.onLoginSuccess();
    } catch (ex) {
      this.onLoginError(ex);
    }
  }

  onLoginSuccess() {
    const changePassword = this.securityService.shouldChangePassword();
    if (changePassword.response === true) {
      this.securityService.password = this.model.password;
      this.dialog.open(DefaultPasswordDialogComponent, {data: changePassword.reason});
      this.router.navigate([changePassword.redirectUrl || this.returnUrl]);
      this.alertService.error(changePassword.reason, true);
    } else {
      this.router.navigate([this.returnUrl]);
    }
  }

  onLoginError(error) {
    let message;
    switch (error.status) {
      case Server.HTTP_UNAUTHORIZED:
      case Server.HTTP_FORBIDDEN:
        const forbiddenCode = error.error.message;
        switch (forbiddenCode) {
          case Server.USER_INACTIVE:
            message = 'The user is inactive. Please contact your administrator.';
            break;
          case Server.USER_SUSPENDED:
            message = 'The user is suspended. Please try again later or contact your administrator.';
            break;
          case Server.PASSWORD_EXPIRED:
            message = 'The user password has expired. Please contact your administrator.';
            break;
          default:
            message = 'The username/password combination you provided is not valid. Please try again or contact your administrator.';
            break;
        }
        break;
      case Server.HTTP_GATEWAY_TIMEOUT:
      case Server.HTTP_NOTFOUND:
        message = 'Unable to login. Domibus is not running.';
        break;
      default:
        this.alertService.exception('Error authenticating:', error);
        return;
    }
    this.alertService.error(message);
  }

}
