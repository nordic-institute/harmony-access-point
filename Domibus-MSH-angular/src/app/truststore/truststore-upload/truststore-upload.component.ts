import {Component, EventEmitter, Output, ViewChild} from "@angular/core";
import {MdDialogRef} from "@angular/material";
import {TrustStoreService} from "../trustore.service";
import {AlertService} from "../../common/alert/alert.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

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

  @ViewChild('fileInput') fileInput;

  constructor(public dialogRef: MdDialogRef<TrustStoreUploadComponent>,
              private truststoreService: TrustStoreService,
              private alertService: AlertService,
              private fb: FormBuilder) {
    this.truststoreForm = fb.group({
      'password': new FormControl("", Validators.required),
    });
  }

  public validateFileSelection() {
    this.fileSelected = this.fileInput.nativeElement.files.length != 0;
  }

  public isFormValid(): boolean {
    return this.truststoreForm.valid && this.fileSelected;
  }

  public submit() {
    if(this.isFormValid()) {
      const fileToUpload = this.fileInput.nativeElement.files[0];
      this.truststoreService.saveTrustStore(fileToUpload, this.truststoreForm.get('password').value).subscribe(res => {
          this.alertService.success(res.text(), false);
          this.onTruststoreUploaded.emit();
        },
        err => {
          this.alertService.exception(`Error updating truststore file (${fileToUpload.name})`, err, false);
        }
      );
      this.dialogRef.close();
    }
  }
}
