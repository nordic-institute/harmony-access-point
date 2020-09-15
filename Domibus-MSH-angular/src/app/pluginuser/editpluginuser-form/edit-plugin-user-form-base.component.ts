import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {PluginUserRO} from '../support/pluginuser';
import {PluginUserService} from '../support/pluginuser.service';
import {UserState} from '../../user/support/user';
import {EditPopupBaseComponent} from '../../common/edit-popup-base.component';
import {SecurityService} from '../../security/security.service';

const NEW_MODE = 'New Plugin User';
const EDIT_MODE = 'Plugin User Edit';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Base class representing a plugin user edit popup form
 *
 */
@Component({
  template: '',
})

export class EditPluginUserFormBaseComponent extends EditPopupBaseComponent {

  existingRoles = [];
  editMode: boolean;
  formTitle: string;
  user: PluginUserRO;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserPatternMessage = PluginUserService.originalUserPatternMessage;
  public originalUserRequiredMessage = PluginUserService.originalUserRequiredMessage;

  constructor(public dialogRef: MatDialogRef<EditPluginUserFormBaseComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {

    super(dialogRef, data);

    this.existingRoles = data.userroles;
    this.user = data.user;
    this.editMode = this.user.status !== UserState[UserState.NEW];
    this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

  }

  isRoleUserSelected(role): boolean {
    return SecurityService.ROLE_USER === role.value;
  }
}
