import {UserResponseRO, UserState} from './user';
import {AlertService} from '../common/alert/alert.service';
import {Injectable} from '@angular/core';
import {AbstractControl, ValidatorFn} from '@angular/forms';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';

/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService {
  constructor(private alertService: AlertService,
              private securityService: SecurityService,
              private domainService: DomainService) {
  }

  validateUsers(users: UserResponseRO[]): boolean {
    let errorMessage = '';
    errorMessage = errorMessage.concat(this.checkUserNameDuplication(users));
    return this.triggerValidation(errorMessage);
  }

  private checkUserNameDuplication(allUsers: UserResponseRO[]) {
    let errorMessage = '';
    let seen = new Set();
    allUsers.every(function (user) {
      if (seen.size === seen.add(user.userName).size) {
        errorMessage = errorMessage.concat('Duplicate user name for user: ' + user.userName + '.');
        return false;
      }
      return true;
    });
    return errorMessage;
  }

  private triggerValidation(errorMessage: string): boolean {
    if (errorMessage.trim()) {
      this.alertService.clearAlert();
      this.alertService.error(errorMessage);
      return false;
    }
    return true;
  }

  matchPassword(form: AbstractControl) {
    const password = form.get('password').value; // to get value in input tag
    const confirmPassword = form.get('confirmation').value; // to get value in input tag
    if (password && confirmPassword && password !== confirmPassword) {
      form.get('confirmation').setErrors({match: true})
    }
  }

  validateForm() {
    return (form: AbstractControl) => {
      this.matchPassword(form);
      this.validateDomainOnAdd(form);
    };
  }

  validateDomainOnAdd(form: AbstractControl) {
    if (!form.get('domain')) {
      return;
    }
    const role = form.get('roles').value;
    if (role && role !== SecurityService.ROLE_AP_ADMIN) {
      const domain = form.get('domain').value;
      this.domainService.getCurrentDomain()
        .subscribe((currDomain) => {
          form.get('domain').setErrors({domain: domain && currDomain && domain !== currDomain.code})
        });
    }

  }


}

