import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../../common/alert/alert.service';
import {FileUploadValidatorService} from '../../common/file-upload-validator.service';

@Component({
  selector: 'app-pmode-upload',
  templateUrl: './pmode-upload.component.html',
  styleUrls: ['../support/pmode.component.css']
})
export class PmodeUploadComponent implements OnInit {

  private url = 'rest/pmode';
  submitInProgress = false;
  description = '';
  useFileSelector = true;

  @ViewChild('fileInput', {static: false})
  private fileInput;

  constructor(@Inject(MAT_DIALOG_DATA) private data: { pModeContents: string },
              public dialogRef: MatDialogRef<PmodeUploadComponent>,
              private http: HttpClient, private alertService: AlertService,
              private fileUploadService: FileUploadValidatorService) {
  }

  ngOnInit() {
    this.useFileSelector = !this.data || !this.data.pModeContents;
  }

  private hasFile(): boolean {
    return (this.useFileSelector && this.fileInput && this.fileInput.nativeElement && this.fileInput.nativeElement.files.length !== 0)
      || (!this.useFileSelector && !!this.data.pModeContents);
  }

  private getFile(): Blob {
    if (this.useFileSelector) {
      return this.fileInput.nativeElement.files[0];
    } else {
      return new Blob([this.data.pModeContents], {type: 'text/xml'});
    }
  }

  public async submit() {
    if (this.submitInProgress) {
      return;
    }
    this.submitInProgress = true;

    try {
      const file = this.getFile();
      await this.fileUploadService.validateFileSize(file);
      if (file.type !== 'text/xml') {
        throw new Error('The file type should be xml.');
      }

      let input = new FormData();
      input.append('file', file);
      input.append('description', (this.description || '').trim().replace(/\t/g, ' '));

      const res = await this.http.post<string>(this.url, input).toPromise();

      this.alertService.success(res, 0);
      this.dialogRef.close({done: true});
      this.submitInProgress = false;
    } catch (err) {
      this.processError(err);
    }
  }

  private processError(err) {
    this.alertService.exception('Error uploading the PMode:', err);
    this.dialogRef.close({done: false});
    this.submitInProgress = false;
  }

  public cancel() {
    this.dialogRef.close({done: false})
  }

  canUpload() {
    return this.hasFile() && this.description.length !== 0 && !this.submitInProgress;
  }
}
