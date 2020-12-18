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

}
