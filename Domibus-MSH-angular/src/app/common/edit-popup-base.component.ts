import {Component, Inject, ViewChild} from '@angular/core';
import {AbstractControl, FormGroup, NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  template: '',
})

export abstract class EditPopupBaseComponent {

  @ViewChild('editForm', {static: false})
  public editForm: NgForm | FormGroup;

  protected constructor(public dialogRef: MatDialogRef<any>, @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  onSubmitForm() {
  }

  public submitForm() {
    console.log('submitForm')
    if (this.isFormDisabled()) {
      return;
    }

    this.onSubmitForm();

    this.dialogRef.close(true);
  }

  public shouldShowErrors(field: NgControl | NgForm | AbstractControl): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }

  public isFormDisabled() {
    return !this.editForm || this.editForm.invalid || !this.editForm.dirty;
  }

}
