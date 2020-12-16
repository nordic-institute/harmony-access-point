import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {TrustStoreService} from './support/trustore.service';
import {TruststoreDialogComponent} from './truststore-dialog/truststore-dialog.component';
import {MatDialog} from '@angular/material';
import {TrustStoreUploadComponent} from './truststore-upload/truststore-upload.component';
import {AlertService} from '../common/alert/alert.service';
import {HttpClient} from '@angular/common/http';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';
import {TrustStoreEntry} from './support/trustore.model';
import {ComponentName} from '../common/component-name-decorator';
import {TruststoreComponent} from './truststore.component';

@Component({
  selector: 'app-tls-truststore',
  templateUrl: './truststore.component.html',
  providers: [TrustStoreService]
})
@ComponentName('TLSTrustStore')
export class TLSTruststoreComponent extends TruststoreComponent implements OnInit {

  constructor(applicationService: ApplicationContextService, http: HttpClient, trustStoreService: TrustStoreService,
              dialog: MatDialog, alertService: AlertService, changeDetector: ChangeDetectorRef) {
    super(applicationService, http, trustStoreService, dialog, alertService, changeDetector);

    // this.TRUSTSTORE_URL = 'rest/truststore/tls';
    this.TRUSTSTORE_CSV_URL = this.TRUSTSTORE_URL + '/tls/csv';
    this.TRUSTSTORE_DOWNLOAD_URL = this.TRUSTSTORE_URL + '/tls';
    this.TRUSTSTORE_UPLOAD_URL = this.TRUSTSTORE_URL + '/tls';
    this.TRUSTSTORE_LIST_ENTRIES_URL = this.TRUSTSTORE_URL + '/tls/entries';
  }

  ngOnInit(): void {
    super.ngOnInit();
  }

}
