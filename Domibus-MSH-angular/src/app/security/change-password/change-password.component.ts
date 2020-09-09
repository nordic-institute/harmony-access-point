import {Component, OnInit, ViewChild} from '@angular/core';
import {AbstractControl, NgControl, NgForm} from '@angular/forms';
import {UserValidatorService} from '../../user/support/uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../../common/alert/alert.service';
import {Router} from '@angular/router';

@Component({
  templateUrl: './change-password.component.html',
  providers: [UserValidatorService]
})

export class ChangePasswordComponent implements OnInit {

  currentPassword: string;
  password: string;
  passwordConfirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;

  @ViewChild('userForm', {static: false})
  public userForm: NgForm;

  constructor(private securityService: SecurityService, private http: HttpClient,
              private alertService: AlertService, private router: Router) {

    this.currentPassword = this.securityService.password;
    this.securityService.password = null;
  }

  async ngOnInit() {
    let passwordPolicy;
    if (!this.securityService.isCurrentUserAdmin()) {
      passwordPolicy = await this.securityService.getPasswordPolicy();
    } else {
      const role = this.securityService.getCurrentUser().authorities[0];
      passwordPolicy = await this.securityService.getPasswordPolicyForUserRole(role);
    }
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  async submitForm() {
    const params = {
      currentPassword: this.currentPassword,
      newPassword: this.password
    };

    try {
      await this.securityService.changePassword(params);
      this.alertService.success('Password successfully changed.');
      this.router.navigate(['/']);
    } catch (error) {
      this.alertService.exception('Password could not be changed.', error);
    }
  }

  public shouldShowErrors(field: NgControl | NgForm | AbstractControl): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }

  public isFormDisabled() {
    return !this.userForm || this.userForm.invalid || !this.userForm.dirty;
  }

}
