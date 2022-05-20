import {Component, Inject, ViewChild} from '@angular/core';
import {MatDialogRef} from '@angular/material';
import {TrustStoreService} from '../support/trustore.service';
import {AbstractControl, FormBuilder, FormControl, FormGroup, NgControl, NgForm, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

@Component({
  selector: 'app-certificate-upload',
  templateUrl: './certificate-upload.component.html',
  styleUrls: ['./certificate-upload.component.css'],
  providers: [TrustStoreService]
})
export class CertificateUploadComponent {

  truststoreForm: FormGroup;
  selectedFileName: string;
  fileSelected = false;

  @ViewChild('fileInput', {static: false}) fileInput;

  constructor(public dialogRef: MatDialogRef<CertificateUploadComponent>, private fb: FormBuilder, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.truststoreForm = fb.group({
      'alias': new FormControl('', Validators.required),
    });
  }

  public isFormValid(): boolean {
    return this.truststoreForm.valid && this.fileSelected;
  }

  public async submit() {
    if (!this.isFormValid()) {
      return;
    }
    const fileToUpload = this.fileInput.nativeElement.files[0];
    const alias = this.truststoreForm.get('alias').value;
    const result = {
      file: fileToUpload,
      alias: alias
    };
    this.dialogRef.close(result);
  }

  selectFile() {
    const fi = this.fileInput.nativeElement;
    const file = fi.files[0];
    this.selectedFileName = file.name;

    this.fileSelected = fi.files.length != 0;
  }

  public shouldShowErrors(field: NgControl | NgForm | AbstractControl): boolean {
    return (field.touched || field.dirty) && !!field.errors;
  }
}
