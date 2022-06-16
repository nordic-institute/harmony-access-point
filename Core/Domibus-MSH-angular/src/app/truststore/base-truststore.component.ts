import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
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
import {FileUploadValidatorService} from '../common/file-upload-validator.service';
import {ComponentType} from 'angular-md2';

@Component({
  selector: 'app-base-truststore',
  templateUrl: './base-truststore.component.html',
  styleUrls: ['./base-truststore.component.css'],
  providers: [TrustStoreService]
})
@ComponentName('TrustStore')
export class BaseTruststoreComponent extends mix(BaseListComponent).with(ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  protected BASE_URL: string;
  protected CSV_URL: string;
  protected DOWNLOAD_URL: string;
  protected UPLOAD_URL: string;
  protected LIST_ENTRIES_URL: string;
  protected ADD_CERTIFICATE_URL: string;
  protected REMOVE_CERTIFICATE_URL: string;

  protected canHandleCertificates: boolean = false;
  protected storeExists: boolean;

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, protected trustStoreService: TrustStoreService,
              public dialog: MatDialog, public alertService: AlertService, private changeDetector: ChangeDetectorRef,
              private fileUploadValidatorService: FileUploadValidatorService, protected truststoreService: TrustStoreService) {
    super();
  }

  ngOnInit(): void {
    super.ngOnInit();

    this.storeExists = false;
    this.loadServerData();
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'Name',
        prop: 'name'
      },
      {
        name: 'Subject',
        prop: 'subject',
      },
      {
        name: 'Issuer',
        prop: 'issuer',
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Valid from',
        prop: 'validFrom'

      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Valid until',
        prop: 'validUntil',
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Name', 'Subject', 'Issuer', 'Valid from', 'Valid until'].indexOf(col.name) !== -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  public async getDataAndSetResults(): Promise<any> {
    return this.getTrustStoreEntries();
  }

  async getTrustStoreEntries() {
    const trustStoreEntries: TrustStoreEntry[] = await this.trustStoreService.getEntries(this.LIST_ENTRIES_URL);

    this.storeExists = true;

    for (const el of trustStoreEntries) {
      var dateDiffToExpiry = (new Date(el.validUntil).getTime() - new Date().getTime());
      var certificateExpiryAlertDaysInMillis = (el.certificateExpiryAlertDays * 24 * 60 * 60 * 1000);
      if ((dateDiffToExpiry > 0) && (dateDiffToExpiry < certificateExpiryAlertDaysInMillis)) {
        el.isAboutToExpire = true;
      } else if (dateDiffToExpiry <= 0) {
        el.isExpired = true;
      }
    }

    super.rows = trustStoreEntries;
    super.count = trustStoreEntries ? trustStoreEntries.length : 0;
  }

  showDetails(selectedRow: any) {
    this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}})
      .afterClosed().subscribe(result => {
    });
  }

  async uploadTrustStore() {
    const comp: ComponentType<unknown> = TrustStoreUploadComponent;
    await this.uploadFile(comp, this.UPLOAD_URL);
  }

  downloadCurrentTrustStore() {
    super.isLoading = true;
    this.http.get(this.DOWNLOAD_URL, {responseType: 'blob', observe: 'response'})
      .subscribe(res => {
        this.trustStoreService.saveTrustStoreFile(res.body);
        super.isLoading = false;
      }, err => {
        super.isLoading = false;
        this.alertService.exception('Error downloading TrustStore:', err);
      });
  }

  get csvUrl(): string {
    return this.CSV_URL;
  }

  getRowClass(row) {
    if (row.isAboutToExpire) {
      return {
        'warnNearExpiry': true
      }
    }
    return {
      'highlighted-row': row.isExpired
    };
  }

  canDownload(): boolean {
    return this.rows && this.rows.length > 0 && !this.isBusy();
  }

  canUpload() {
    return !this.isBusy();
  }

  public showCertificateOperations() {
    return this.canHandleCertificates;
  }

  protected async uploadFile(comp: ComponentType<unknown>, url: string) {
    let params = await this.dialog.open(comp).afterClosed().toPromise();
    if (params != null) {
      try {
        super.isLoading = true;
        await this.fileUploadValidatorService.validateFileSize(params.file);

        let res = await this.truststoreService.uploadFile(url, params);
        this.alertService.success(res);

        await this.getTrustStoreEntries();
      } catch (err) {
        this.alertService.exception(`Error updating truststore file (${params.file.name})`, err);
      } finally {
        super.isLoading = false;
      }
    }
  }

  reloadKeyStore() {
  }
}
