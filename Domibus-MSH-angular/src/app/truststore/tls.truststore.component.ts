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
@ComponentName('TLSTrustStore')
export class TLSTruststoreComponent extends BaseTruststoreComponent implements OnInit {

  constructor(applicationService: ApplicationContextService, http: HttpClient, trustStoreService: TrustStoreService,
              dialog: MatDialog, alertService: AlertService, changeDetector: ChangeDetectorRef,
              fileUploadValidatorService: FileUploadValidatorService, truststoreService: TrustStoreService) {
    super(applicationService, http, trustStoreService, dialog, alertService, changeDetector, fileUploadValidatorService, trustStoreService);

    this.TRUSTSTORE_URL = 'rest/tlstruststore';
    this.TRUSTSTORE_CSV_URL = this.TRUSTSTORE_URL + '/csv';
    this.TRUSTSTORE_DOWNLOAD_URL = this.TRUSTSTORE_URL;
    this.TRUSTSTORE_UPLOAD_URL = this.TRUSTSTORE_URL;
    this.TRUSTSTORE_LIST_ENTRIES_URL = this.TRUSTSTORE_URL + '/entries';
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

}
