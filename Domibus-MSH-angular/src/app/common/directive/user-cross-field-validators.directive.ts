import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn} from '@angular/forms';
import {Directive} from '@angular/core';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Behavioural directive responsible for validating password and confirmation;
 * Used as cross fields validation method in template style angular forms
 *
 */
@Directive({
  selector: '[userCrossFieldValidators]',
  providers: [{provide: NG_VALIDATORS, useExisting: UserCrossFieldValidatorsDirective, multi: true}]
})
export class UserCrossFieldValidatorsDirective implements Validator {
  validate(control: AbstractControl): ValidationErrors {
    return crossFieldsValidators()(control);
  }
}

export function crossFieldsValidators(): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    return matchPassword(control);
  };
}

export function matchPassword(form: AbstractControl): { [key: string]: any } | null {
  if (form.get('password') && form.get('confirmation')) {
    const password = form.get('password').value;
    const confirmPassword = form.get('confirmation').value;
    if (password && confirmPassword && password !== confirmPassword) {
      return {match: true};
    } else {
      return null;
    }
  }
}
