import {Component, Inject, ViewChild} from '@angular/core';
import {AbstractControl, FormGroup, NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Base class representing an edit popup form component
 * More common functionality will be added in time
 */

@Component({
  template: '',
})

export abstract class EditPopupBaseComponent {

  @ViewChild('editForm', {static: false})
  public editForm: NgForm | FormGroup;

  protected constructor(public dialogRef: MatDialogRef<any>, @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  // method to be overridden in derived classes to add specific behaviour on submit
  onSubmitForm() {
  }

  public submitForm() {
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
