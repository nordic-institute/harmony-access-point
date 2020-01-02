import {Component, Inject, ViewChild} from '@angular/core';
import {FormGroup, NgControl, NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  template: '',
})

export class EditPopupBaseComponent {

  constructor(public dialogRef: MatDialogRef<any>, @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  @ViewChild('editForm', {static: false})
  public editForm: NgForm | FormGroup;

  public submitForm() {
    if (this.editForm.invalid) {
      return;
    }
    this.dialogRef.close(true);
  }

  public shouldShowErrors(field: NgControl | NgForm): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }

  public isFormDisabled() {
    if (!this.editForm) return true;
    return this.editForm.invalid || !this.editForm.dirty;
  }

}
