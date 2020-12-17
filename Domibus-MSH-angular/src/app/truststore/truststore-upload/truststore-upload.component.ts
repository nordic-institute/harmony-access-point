import {Component, EventEmitter, Output, ViewChild} from '@angular/core';
import {MatDialogRef} from '@angular/material';
import {TrustStoreService} from '../support/trustore.service';
import {AlertService} from '../../common/alert/alert.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {FileUploadValidatorService} from '../../common/file-upload-validator.service';

@Component({
  selector: 'app-truststore-upload',
  templateUrl: './truststore-upload.component.html',
  styleUrls: ['./truststore-upload.component.css'],
  providers: [TrustStoreService]
})
export class TrustStoreUploadComponent {

  truststoreForm: FormGroup;

  fileSelected = false;

  @Output() onTruststoreUploaded: EventEmitter<any> = new EventEmitter();

  @ViewChild('fileInput', {static: false}) fileInput;

  constructor(public dialogRef: MatDialogRef<TrustStoreUploadComponent>,
              private truststoreService: TrustStoreService,
              private alertService: AlertService,
              private fb: FormBuilder, private fileUploadService: FileUploadValidatorService) {
    this.truststoreForm = fb.group({
      'password': new FormControl('', Validators.required),
    });
  }

  public validateFileSelection() {
    this.fileSelected = this.fileInput.nativeElement.files.length != 0;
  }

  public isFormValid(): boolean {
    return this.truststoreForm.valid && this.fileSelected;
  }

  public async submit() {
    if (this.isFormValid()) {
      const fileToUpload = this.fileInput.nativeElement.files[0];
      try {
        await this.fileUploadService.validateFileSize(fileToUpload);

        this.truststoreService.uploadTrustStore(fileToUpload, this.truststoreForm.get('password').value)
          .subscribe(res => {
              this.alertService.success(res);
              this.onTruststoreUploaded.emit();
            },
            err => {
              this.alertService.exception(`Error updating truststore file (${fileToUpload.name})`, err);
            }
          );

      } catch (e) {
        this.alertService.exception('Could not upload the truststore file. ', e);
      }

      this.dialogRef.close();
    }
  }
}
