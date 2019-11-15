import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {TrustStoreService} from './trustore.service';
import {TrustStoreEntry} from './trustore.model';
import {TruststoreDialogComponent} from './truststore-dialog/truststore-dialog.component';
import {MatDialog} from '@angular/material';
import {TrustStoreUploadComponent} from './truststore-upload/truststore-upload.component';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import {AlertService} from '../common/alert/alert.service';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-truststore',
  templateUrl: './truststore.component.html',
  styleUrls: ['./truststore.component.css'],
  providers: [TrustStoreService]
})
export class TruststoreComponent implements OnInit {
  static readonly TRUSTSTORE_URL: string = 'rest/truststore';
  static readonly TRUSTSTORE_CSV_URL: string = TruststoreComponent.TRUSTSTORE_URL + '/csv';
  static readonly TRUSTSTORE_DOWNLOAD_URL: string = TruststoreComponent.TRUSTSTORE_URL + '/download';
  columnPicker: ColumnPickerBase = new ColumnPickerBase();

  rowLimiter: RowLimiterBase = new RowLimiterBase();

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<any>;

  trustStoreEntries: Array<TrustStoreEntry>;
  selectedMessages: Array<any>;
  loading: boolean;

  rows: Array<any> = [];
  offset: number;

  constructor(private http: HttpClient, private trustStoreService: TrustStoreService, public dialog: MatDialog, public alertService: AlertService) {
  }

  ngOnInit(): void {
    this.trustStoreEntries = [];
    this.selectedMessages = [];
    this.rows = [];

    this.offset = 0;

    this.getTrustStoreEntries();
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

  getTrustStoreEntries(): void {
    this.trustStoreService.getEntries().subscribe(trustStoreEntries => {
      this.trustStoreEntries = trustStoreEntries;
      this.offset = 0;
    });
  }

  onSelect({selected}) {
    this.selectedMessages.splice(0, this.selectedMessages.length);
    this.selectedMessages.push(...selected);
  }

  onActivate(event) {
    if ('dblclick' === event.type) {
      this.details(event.row);
    }
  }

  details(selectedRow: any) {
    this.dialog.open(TruststoreDialogComponent, {data: {trustStoreEntry: selectedRow}})
      .afterClosed().subscribe(result => {
    });
  }

  onChangePage(event: any): void {
    this.offset = event.offset;
  }

  changePageSize(newPageSize: number) {
    this.rowLimiter.pageSize = newPageSize;
    this.getTrustStoreEntries();
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
    if (this.trustStoreEntries.length > 0) {
      return true;
    } else
      return false;
  }

  /**
   * Saves the content of the datatable into a CSV file
   */
  saveAsCSV() {
    if (this.trustStoreEntries.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }

    DownloadService.downloadNative(TruststoreComponent.TRUSTSTORE_CSV_URL);
  }

}
