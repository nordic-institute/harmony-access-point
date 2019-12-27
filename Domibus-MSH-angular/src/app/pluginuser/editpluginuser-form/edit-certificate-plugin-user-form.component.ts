import {Component, Inject} from '@angular/core';
import {NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/support/uservalidator.service';
import {PluginUserRO} from '../support/pluginuser';
import {PluginUserService} from '../support/pluginuser.service';
import {UserState} from '../../user/support/user';
import {EditPluginUserFormBaseComponent} from './edit-plugin-user-form-base.component';

const NEW_MODE = 'New PluginUser';
const EDIT_MODE = 'Plugin User Edit';

@Component({
  selector: 'editcertificatepluginuser-form',
  templateUrl: './edit-certificate-plugin-user-form.component.html',
  providers: [UserValidatorService]
})
export class EditCertificatePluginUserFormComponent extends EditPluginUserFormBaseComponent {

  // existingRoles = [];
  // editMode: boolean;
  // formTitle: string;
  // user: PluginUserRO;

  public certificateIdPattern = PluginUserService.certificateIdPattern;
  public certificateIdMessage = PluginUserService.certificateIdMessage;

  // public originalUserPattern = PluginUserService.originalUserPattern;
  // public originalUserMessage = PluginUserService.originalUserMessage;

  constructor(public dialogRef: MatDialogRef<EditCertificatePluginUserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {

    super(dialogRef, data);

    // this.existingRoles = data.userroles;
    // this.user = data.user;
    // this.editMode = this.user.status !== UserState[UserState.NEW];
    // this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;
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
