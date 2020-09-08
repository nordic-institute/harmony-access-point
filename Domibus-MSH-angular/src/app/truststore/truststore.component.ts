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

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css'],
  providers: [TrustStoreService]
})
export class TruststoreComponent extends mix(BaseListComponent)
  .with(ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly TRUSTSTORE_URL: string = 'rest/truststore';
  static readonly TRUSTSTORE_CSV_URL: string = TruststoreComponent.TRUSTSTORE_URL + '/csv';
  static readonly TRUSTSTORE_DOWNLOAD_URL: string = TruststoreComponent.TRUSTSTORE_URL + '/download';

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private trustStoreService: TrustStoreService,
              public dialog: MatDialog, public alertService: AlertService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit(): void {
    super.ngOnInit();

    this.loadServerData();
  }

  public get name(): string {
    return 'TrustStore';
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
    const trustStoreEntries: TrustStoreEntry[] = await this.trustStoreService.getEntries();

    trustStoreEntries.forEach(el => el.isExpired = new Date(el.validUntil) < new Date());

    super.rows = trustStoreEntries;
    super.count = trustStoreEntries ? trustStoreEntries.length : 0;
  }

  showDetails(selectedRow: any) {
    this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}})
      .afterClosed().subscribe(result => {
    });
  }

  openEditTrustStore() {
    this.dialog.open(TrustStoreUploadComponent).componentInstance.onTruststoreUploaded
      .subscribe(updated => {
        this.getTrustStoreEntries();
      });
  }

  /**
   * Method called when Download button or icon is clicked
   */
  downloadCurrentTrustStore() {
    this.http.get(TruststoreComponent.TRUSTSTORE_DOWNLOAD_URL, {responseType: 'blob', observe: 'response'})
      .subscribe(res => {
        this.trustStoreService.saveTrustStoreFile(res.body);
      }, err => {
        this.alertService.exception('Error downloading TrustStore:', err);
      });
  }

  /**
   * Method that checks if 'Download' button should be enabled
   * @returns {boolean} true, if button can be enabled; and false, otherwise
   */
  canDownload(): boolean {
    if (this.rows.length > 0) {
      return true;
    } else {
      return false;
    }
  }

  get csvUrl(): string {
    return TruststoreComponent.TRUSTSTORE_CSV_URL;
  }

  getRowClass(row) {
    return {
      'highlighted-row': row.isExpired
    };
  }

}
