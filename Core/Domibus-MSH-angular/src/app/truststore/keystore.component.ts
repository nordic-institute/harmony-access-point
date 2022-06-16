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
  selector: 'app-keystore',
  templateUrl: './base-truststore.component.html',
  providers: [TrustStoreService]
})
@ComponentName('Domibus KeyStore')
export class KeystoreComponent extends BaseTruststoreComponent implements OnInit {

  constructor(applicationService: ApplicationContextService, http: HttpClient, trustStoreService: TrustStoreService,
              dialog: MatDialog, alertService: AlertService, changeDetector: ChangeDetectorRef,
              fileUploadValidatorService: FileUploadValidatorService) {
    super(applicationService, http, trustStoreService, dialog, alertService, changeDetector, fileUploadValidatorService, trustStoreService);

    this.BASE_URL = 'rest/keystore';
    this.CSV_URL = this.BASE_URL + '/csv';
    this.DOWNLOAD_URL = this.BASE_URL + '/download';
    this.UPLOAD_URL = this.BASE_URL + '/save';
    this.LIST_ENTRIES_URL = this.BASE_URL + '/list';
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

  async reloadKeyStore() {
    try {
      await this.trustStoreService.reloadKeyStore();
      this.alertService.success('Keystore was successfully reset.')
    } catch (ex) {
      this.alertService.exception('Error reseting the keystore:', ex);
    }
  }
}
