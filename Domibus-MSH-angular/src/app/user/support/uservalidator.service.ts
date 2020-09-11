import {UserResponseRO} from './user';
import {AlertService} from '../../common/alert/alert.service';
import {Injectable} from '@angular/core';
import {AbstractControl, ValidatorFn} from '@angular/forms';
import {SecurityService} from '../../security/security.service';
import {DomainService} from '../../security/domain.service';

/**
 * Created by dussath on 6/20/17.
 */
@Injectable()
export class UserValidatorService {

  public static readonly USER_NAME_PATTERN = '[a-zA-Z0-9\.@_]*';
  public static readonly USER_NAME_MINLENGTH_MESSAGE = 'You should type at least 4 characters';
  public static readonly USER_NAME_PATTERN_MESSAGE = 'You should not use special characters';
  public static readonly USER_NAME_REQUIRED_MESSAGE = 'You should type an username';

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

  passwordShouldMatch(): ValidatorFn {
    return (form: AbstractControl) => {
      return this.matchPassword(form);
    };
  }

  matchPassword(form: AbstractControl): { [key: string]: any } | null {
    const password = form.get('password').value;
    const confirmPassword = form.get('confirmation').value;
    if (password && confirmPassword && password !== confirmPassword) {
      return {match: true};
    } else {
      return null;
    }
  }

  defaultDomain(): ValidatorFn {
    return (form: AbstractControl) => {
      let res = {res: null};
      this.validateDomain(form, res);
      return res.res;
    };
  }

  async validateDomain(form: AbstractControl, res: { res: any }) {
    if (!form.get('domain')) {
      return;
    }
    const role = form.get('roles').value;
    if (role && role === SecurityService.ROLE_AP_ADMIN) {
      return;
    }
    const domain = form.get('domain').value;
    this.domainService.getCurrentDomain().subscribe((currDomain) => {
      if (domain && currDomain && domain !== currDomain.code) {
        res.res = {domain: true};
      } else {
        res.res = null;
      }
    });
  }

}

