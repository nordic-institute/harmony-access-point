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
import {FileUploadValidatorService} from '../common/file-upload-validator.service';

@Component({
  selector: 'app-base-truststore',
  templateUrl: './base-truststore.component.html',
  styleUrls: ['./base-truststore.component.css'],
  providers: [TrustStoreService]
})
@ComponentName('TrustStore')
export class BaseTruststoreComponent extends mix(BaseListComponent)
  .with(ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  protected TRUSTSTORE_URL = 'rest/truststore';
  protected TRUSTSTORE_CSV_URL: string;
  protected TRUSTSTORE_DOWNLOAD_URL: string;
  protected TRUSTSTORE_UPLOAD_URL: string;
  protected TRUSTSTORE_LIST_ENTRIES_URL: string;

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private trustStoreService: TrustStoreService,
              public dialog: MatDialog, public alertService: AlertService, private changeDetector: ChangeDetectorRef,
              private fileUploadValidatorService: FileUploadValidatorService, private truststoreService: TrustStoreService) {
    super();
  }

  ngOnInit(): void {
    super.ngOnInit();

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
    const trustStoreEntries: TrustStoreEntry[] = await this.trustStoreService.getEntries(this.TRUSTSTORE_LIST_ENTRIES_URL);

    trustStoreEntries.forEach(el => el.isExpired = new Date(el.validUntil) < new Date());

    super.rows = trustStoreEntries;
    super.count = trustStoreEntries ? trustStoreEntries.length : 0;
  }

  showDetails(selectedRow: any) {
    this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}})
      .afterClosed().subscribe(result => {
    });
  }

  async uploadTrustStore() {
    let params = await this.dialog.open(TrustStoreUploadComponent).afterClosed().toPromise();
    if (params != null) {
      try {
        super.isLoading = true;
        await this.fileUploadValidatorService.validateFileSize(params.fileToUpload);

        let res = await this.truststoreService.uploadTrustStore(this.TRUSTSTORE_UPLOAD_URL, params.fileToUpload, params.password).toPromise();
        this.alertService.success(res);

        await this.getTrustStoreEntries();
      } catch (err) {
        this.alertService.exception(`Error updating truststore file (${params.fileToUpload.name})`, err);
      } finally {
        super.isLoading = false;
      }
    }
  }

  /**
   * Method called when Download button or icon is clicked
   */
  downloadCurrentTrustStore() {
    super.isLoading = true;
    this.http.get(this.TRUSTSTORE_DOWNLOAD_URL, {responseType: 'blob', observe: 'response'})
      .subscribe(res => {
        this.trustStoreService.saveTrustStoreFile(res.body);
        super.isLoading = false;
      }, err => {
        super.isLoading = false;
        this.alertService.exception('Error downloading TrustStore:', err);
      });
  }

  get csvUrl(): string {
    return this.TRUSTSTORE_CSV_URL;
  }

  getRowClass(row) {
    return {
      'highlighted-row': row.isExpired
    };
  }

  canDownload(): boolean {
    return (this.rows.length > 0) && !this.isBusy();
  }

  canUpload() {
    return !this.isBusy();
  }

}
