import {Component, EventEmitter, Inject, Output, ViewChild} from '@angular/core';
import {MatDialogRef} from '@angular/material';
import {TrustStoreService} from '../support/trustore.service';
import {AlertService} from '../../common/alert/alert.service';
import {AbstractControl, FormBuilder, FormControl, FormGroup, NgControl, NgForm, Validators} from '@angular/forms';
import {FileUploadValidatorService} from '../../common/file-upload-validator.service';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

@Component({
  selector: 'app-truststore-upload',
  templateUrl: './truststore-upload.component.html',
  styleUrls: ['./truststore-upload.component.css'],
  providers: [TrustStoreService]
})
export class TrustStoreUploadComponent {

  truststoreForm: FormGroup;
  selectedFileName: string;
  fileSelected = false;

  // @Output() onTruststoreUploaded: EventEmitter<any> = new EventEmitter();

  @ViewChild('fileInput', {static: false}) fileInput;

  constructor(public dialogRef: MatDialogRef<TrustStoreUploadComponent>,
              private truststoreService: TrustStoreService,
              private alertService: AlertService,
              private fb: FormBuilder, private fileUploadService: FileUploadValidatorService,
              @Inject(MAT_DIALOG_DATA) public data: any) {

    this.truststoreForm = fb.group({
      'password': new FormControl('', Validators.required),
    });
  }

  public isFormValid(): boolean {
    return this.truststoreForm.valid && this.fileSelected;
  }

  public async submit() {
    if (this.isFormValid()) {
      const fileToUpload = this.fileInput.nativeElement.files[0];
      // try {
      //   await this.fileUploadService.validateFileSize(fileToUpload);
      //
      //   this.truststoreService.uploadTrustStore(this.data.url, fileToUpload, this.truststoreForm.get('password').value)
      //     .subscribe(res => {
      //         this.alertService.success(res);
      //         // this.onTruststoreUploaded.emit();
      //       },
      //       err => {
      //         this.alertService.exception(`Error updating truststore file (${fileToUpload.name})`, err);
      //       }
      //     );
      //
      // } catch (e) {
      //   this.alertService.exception('Could not upload the truststore file. ', e);
      // }

      this.dialogRef.close({
        fileToUpload: fileToUpload,
        password: this.truststoreForm.get('password').value
      });
    }
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
