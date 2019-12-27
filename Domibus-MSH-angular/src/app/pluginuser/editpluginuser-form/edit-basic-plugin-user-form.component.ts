import {Component, Inject, OnInit} from '@angular/core';
import {NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/support/uservalidator.service';
import {PluginUserRO} from '../support/pluginuser';
import {PluginUserService} from '../support/pluginuser.service';
import {SecurityService} from '../../security/security.service';
import {UserState} from '../../user/support/user';
import {EditPluginUserFormBaseComponent} from './edit-plugin-user-form-base.component';

const NEW_MODE = 'New Plugin User';
const EDIT_MODE = 'Plugin User Edit';

@Component({
  selector: 'editbasicpluginuser-form',
  templateUrl: './edit-basic-plugin-user-form.component.html',
  providers: [UserValidatorService]
})

export class EditBasicPluginUserFormComponent extends EditPluginUserFormBaseComponent implements OnInit {

  // existingRoles = [];
  // editMode: boolean;
  // formTitle: string;
  // user: PluginUserRO;

  passwordConfirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;

  // public originalUserPattern = PluginUserService.originalUserPattern;
  // public originalUserMessage = PluginUserService.originalUserMessage;

  constructor(public dialogRef: MatDialogRef<EditBasicPluginUserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private securityService: SecurityService) {

    super(dialogRef, data);

    // this.existingRoles = data.userroles;
    // this.user = data.user;
    // this.editMode = this.user.status !== UserState[UserState.NEW];
    // this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

    this.passwordConfirmation = data.user.password;
  }

  async ngOnInit() {
    const passwordPolicy = await this.securityService.getPluginPasswordPolicy();
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  // submitForm(userForm: NgForm) {
  //   if (userForm.invalid) {
  //     return;
  //   }
  //   this.dialogRef.close(true);
  // }
  //
  // shouldShowErrors(field: NgControl | NgForm): boolean {
  //   return (field.touched || field.dirty) && !!field.errors;
  // }
  //
  // isFormDisabled(form: NgForm) {
  //   return form.invalid || !form.dirty;
  // }

}
