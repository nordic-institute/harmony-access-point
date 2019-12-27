import {Component, Inject, OnInit} from '@angular/core';
import {FormGroup, NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {UserValidatorService} from '../../user/uservalidator.service';
import {PluginUserRO} from '../pluginuser';
import {PluginUserService} from '../pluginuser.service';
import {SecurityService} from '../../security/security.service';
import {UserState} from '../../user/user';

const NEW_MODE = 'New Plugin User';
const EDIT_MODE = 'Plugin User Edit';

@Component({
  selector: 'editbasicpluginuser-form',
  templateUrl: './editbasicpluginuser-form.component.html',
  providers: [UserValidatorService]
})

export class EditbasicpluginuserFormComponent implements OnInit {

  existingRoles = [];
  passwordConfirmation: string;
  public passwordPattern: string;
  public passwordValidationMessage: string;
  editMode: boolean;
  formTitle: string;
  userForm: FormGroup;
  user: PluginUserRO;

  public originalUserPattern = PluginUserService.originalUserPattern;
  public originalUserMessage = PluginUserService.originalUserMessage;

  // public certificateIdPattern = PluginUserService.certificateIdPattern;
  // public certificateIdMessage = PluginUserService.certificateIdMessage;

  constructor(public dialogRef: MatDialogRef<EditbasicpluginuserFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private securityService: SecurityService) {

    this.existingRoles = data.userroles;
    this.user = data.user;
    this.editMode = this.user.status !== UserState[UserState.NEW];

    this.passwordConfirmation = data.user.password;

    this.formTitle = this.editMode ? EDIT_MODE : NEW_MODE;

    // if (this.editMode) {
    //   this.userForm = fb.group({
    //     'userName': new FormControl({value: this.user.userName, disabled: true}, Validators.nullValidator),
    //     'originalUser': new FormControl(this.user.originalUser, null),
    //     'role': new FormControl(this.user.authRoles, Validators.required),
    //     'password': [null],
    //     'confirmation': [null],
    //     'active': new FormControl(this.user.active, Validators.required)
    //   }, {
    //     validator: userValidatorService.validateForm()
    //   });
    // } else {
    //   this.userForm = fb.group({
    //     'userName': new FormControl(this.user.userName, Validators.required),
    //     'originalUser': new FormControl(this.user.originalUser, null),
    //     'role': new FormControl(this.user.authRoles, Validators.required),
    //     'password': [Validators.required, Validators.pattern],
    //     'confirmation': [Validators.required],
    //     'active': [Validators.required]
    //   }, {
    //     validator: userValidatorService.validateForm()
    //   });
    // }

  }

  async ngOnInit() {
    const passwordPolicy = await this.securityService.getPluginPasswordPolicy();
    this.passwordPattern = passwordPolicy.pattern;
    this.passwordValidationMessage = passwordPolicy.validationMessage;
  }

  submitForm(userForm: NgForm) {
    if (userForm.invalid) {
      return;
    }
    this.dialogRef.close(true);
  }

  shouldShowErrors(field: NgControl | NgForm): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }

}
