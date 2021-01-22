import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {TrustStoreService} from './support/trustore.service';
import {MatDialog} from '@angular/material';
import {AlertService} from '../common/alert/alert.service';
import {HttpClient} from '@angular/common/http';
import {ApplicationContextService} from '../common/application-context.service';
import {ComponentName} from '../common/component-name-decorator';
import {BaseTruststoreComponent} from './base-truststore.component';
import {FileUploadValidatorService} from '../common/file-upload-validator.service';
import {ComponentType} from 'angular-md2';
import {CertificateUploadComponent} from './certificate-upload/certificate-upload.component';

@Component({
  selector: 'app-tls-truststore',
  templateUrl: './base-truststore.component.html',
  providers: [TrustStoreService]
})
@ComponentName('TLS TrustStore')
export class TLSTruststoreComponent extends BaseTruststoreComponent implements OnInit {

  constructor(applicationService: ApplicationContextService, http: HttpClient, trustStoreService: TrustStoreService,
              dialog: MatDialog, alertService: AlertService, changeDetector: ChangeDetectorRef,
              fileUploadValidatorService: FileUploadValidatorService, truststoreService: TrustStoreService) {
    super(applicationService, http, trustStoreService, dialog, alertService, changeDetector, fileUploadValidatorService, trustStoreService);

    this.BASE_URL = 'rest/tlstruststore';
    this.CSV_URL = this.BASE_URL + '/entries/csv';
    this.DOWNLOAD_URL = this.BASE_URL;
    this.UPLOAD_URL = this.BASE_URL;
    this.LIST_ENTRIES_URL = this.BASE_URL + '/entries';
    this.ADD_CERTIFICATE_URL = this.BASE_URL + '/entries';
    this.REMOVE_CERTIFICATE_URL = this.BASE_URL + '/entries/alias';

    this.canHandleCertificates = true;
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

  canAddCertificate() {
    return this.rows && this.rows.length > 0 && !this.isBusy();
  }

  canRemoveCertificate() {
    return this.selected.length == 1 && !this.isBusy();
  }

  async addCertificate() {
    const comp: ComponentType<unknown> = CertificateUploadComponent;
    this.uploadFile(comp, this.ADD_CERTIFICATE_URL);
  }

  async removeCertificate() {
    const cert = this.selected[0];
    if (!cert) {
      return;
    }
    try {
      super.isLoading = true;
      let res = await this.truststoreService.removeCertificate(this.REMOVE_CERTIFICATE_URL, cert);
      this.alertService.success(res);
    } catch (err) {
      this.alertService.exception(`Error removing the certificate (${cert.name}) from truststore.`, err);
    } finally {
      super.isLoading = false;
      this.loadServerData();
    }
  }
}
