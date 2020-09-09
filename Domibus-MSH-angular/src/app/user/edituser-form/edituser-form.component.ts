import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {UserValidatorService} from '../support/uservalidator.service';
import {SecurityService} from '../../security/security.service';
import {UserService} from '../support/user.service';
import {DomainService} from '../../security/domain.service';
import {UserResponseRO, UserState} from '../support/user';
import {PasswordPolicyRO} from '../../security/passwordPolicyRO';

const NEW_MODE = 'New User';
const EDIT_MODE = 'User Edit';

@Component({
  selector: 'edituser-form',
  templateUrl: 'edituser-form.component.html',
})

export class EditUserComponent implements OnInit {
  user: UserResponseRO;
  existingRoles = [];
  existingDomains = [];
  currentDomain: string;
  confirmation: string;
  public emailPattern = '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{1,}';
  public passwordPattern: string;
  public passwordValidationMessage: string;
  isDomainVisible: boolean;
  formTitle: string;
  userForm: FormGroup;

  @ViewChild('user_name', {static: false}) user_name: ElementRef;

  constructor(public dialogRef: MatDialogRef<EditUserComponent>, @Inject(MAT_DIALOG_DATA) public data: any,
              private fb: FormBuilder, private userValidatorService: UserValidatorService,
              private userService: UserService, private securityService: SecurityService, private domainService: DomainService) {
    this.existingRoles = data.userroles;
    this.existingDomains = data.userdomains;
    this.user = data.user;
    this.confirmation = data.user.password;
  }

  async ngOnInit() {
    await this.additionalSetUp();
    this.buildFormControls();

    this.userForm.valueChanges.subscribe((changes) => {
      this.updateModel(changes);
    });

    setTimeout(() => this.user_name.nativeElement.focus(), 1000);
  }

  private updateModel(changes) {
    delete changes.confirmation;
    Object.assign(this.user, changes);
  }

  private buildFormControls() {
    this.userForm = this.fb.group({
      'userName': new FormControl({
        value: this.user.userName,
        disabled: !this.isNewUser()
      }, [Validators.required, Validators.maxLength(255), Validators.minLength(4), Validators.pattern('[a-zA-Z0-9\.@_]*')]),
      'email': new FormControl(this.user.email, [Validators.pattern(this.emailPattern), Validators.maxLength(255)]),
      'roles': new FormControl(this.user.roles, Validators.required),
      'domain': new FormControl({value: this.user.domain, disabled: this.isDomainDisabled()}, Validators.required),
      'password': new FormControl(this.user.password),
      'confirmation': new FormControl(this.confirmation),
      'active': new FormControl({value: this.user.active, disabled: this.isCurrentUser()}, Validators.required)
    }, {
      validator: [this.userValidatorService.passwordShouldMatch(), this.userValidatorService.defaultDomain()]
    });
  }

  private isNewUser() {
    return this.user.status === UserState[UserState.NEW];
  }

  private async additionalSetUp() {
    this.domainService.getCurrentDomain().subscribe((dom) => {
      this.currentDomain = dom.code;
    });
    this.isDomainVisible = await this.userService.isDomainVisible();
    if (!this.isNewUser()) {
      this.existingRoles = this.getAllowedRoles(this.existingRoles, this.user.roles);
    }

    this.formTitle = this.isNewUser() ? NEW_MODE : EDIT_MODE;
  }

  isCurrentUser(): boolean {
    let currentUser = this.securityService.getCurrentUser();
    return currentUser && currentUser.username === this.user.userName;
  }

  isDomainDisabled() {
    // if the edited user is not super-user
    return this.user.roles !== SecurityService.ROLE_AP_ADMIN;
  }

  async onRoleChange($event) {
    const role: string = $event.value;
    const dimainCtrl = this.userForm.get('domain');
    if (role === SecurityService.ROLE_AP_ADMIN) {
      dimainCtrl.enable();
    } else {
      dimainCtrl.disable();
      this.userForm.patchValue({domain: this.currentDomain});
    }

    await this.getPasswordPolicy(role);
    this.setPasswordValidators();
  }

  // filters out roles so that the user cannot change from ap admin to the other 2 roles or vice-versa
  getAllowedRoles(allRoles, userRole) {
    if (userRole === SecurityService.ROLE_AP_ADMIN) {
      return [SecurityService.ROLE_AP_ADMIN];
    } else {
      return allRoles.filter(role => role !== SecurityService.ROLE_AP_ADMIN);
    }
  }

  submitForm() {
    if (this.userForm.valid) {
      this.dialogRef.close(true);
    }
  }

  shouldShowErrorsForFieldNamed(fieldName: string): boolean {
    let field = this.userForm.get(fieldName);
    return (field.touched || field.dirty) && !!field.errors;
  }

  isFormDisabled() {
    return this.userForm.invalid || !this.userForm.dirty;
  }

  private async getPasswordPolicy(role: string): Promise<PasswordPolicyRO> {
    const passwordPolicy = await this.securityService.getPasswordPolicyForUserRole(role);
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
    return passwordPolicy;
  }

  private setPasswordValidators() {
    const passCtrl = this.userForm.get('password');
    passCtrl.setValidators([Validators.pattern(this.passwordPattern), this.isNewUser() ? Validators.required : Validators.nullValidator]);
    passCtrl.updateValueAndValidity();

    const confPassCtrl = this.userForm.get('confirmation');
    confPassCtrl.setValidators([Validators.pattern(this.passwordPattern), this.isNewUser() ? Validators.required : Validators.nullValidator]);
    confPassCtrl.updateValueAndValidity();
  }
}
