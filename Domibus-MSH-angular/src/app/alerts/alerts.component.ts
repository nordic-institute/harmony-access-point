import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import {Observable} from 'rxjs/Observable';
import {AlertsResult} from './alertsresult';
import {Http, Response, URLSearchParams} from '@angular/http';
import {AlertService} from '../common/alert/alert.service';
import {CancelDialogComponent} from '../common/cancel-dialog/cancel-dialog.component';
import {MdDialog} from '@angular/material';
import {SaveDialogComponent} from '../common/save-dialog/save-dialog.component';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import SortableListMixin from '../common/mixins/sortable-list.mixin';
import {DirtyOperations} from '../common/dirty-operations';
import {isNullOrUndefined} from 'util';
import {AlertsEntry} from './alertsentry';
import {showOnDirtyErrorStateMatcher, ErrorStateMatcher} from '@angular/material';

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
  providers: []
})

export class AlertsComponent extends mix(BaseListComponent).with(FilterableListMixin, SortableListMixin) implements OnInit, DirtyOperations {
  static readonly ALERTS_URL: string = 'rest/alerts';
  static readonly ALERTS_CSV_URL: string = AlertsComponent.ALERTS_URL + '/csv';
  static readonly ALERTS_TYPES_URL: string = AlertsComponent.ALERTS_URL + '/types';
  static readonly ALERTS_STATUS_URL: string = AlertsComponent.ALERTS_URL + '/status';
  static readonly ALERTS_LEVELS_URL: string = AlertsComponent.ALERTS_URL + '/levels';
  static readonly ALERTS_PARAMS_URL: string = AlertsComponent.ALERTS_URL + '/params';

  @ViewChild('rowProcessed') rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithSpaceAfterCommaTpl') public rowWithSpaceAfterCommaTpl: TemplateRef<any>;

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  public rowLimiter: RowLimiterBase;

  advancedSearch: boolean;
  loading: boolean;

  // data table
  rows: Array<AlertsEntry>;
  count: number;
  offset: number;

  isChanged: boolean;

  aTypes: Array<any>;
  aStatuses: Array<any>;
  aLevels: Array<any>;

  aProcessedValues = ['PROCESSED', 'UNPROCESSED'];

  dynamicFilters: Array<any>;
  dynamicDatesFilter: any;
  nonDateParameters: Array<any>;
  alertTypeWithDate: boolean;

  timestampCreationFromMaxDate: Date;
  timestampCreationToMinDate: Date;
  timestampCreationToMaxDate: Date;
  timestampReportingFromMaxDate: Date;
  timestampReportingToMinDate: Date;
  timestampReportingToMaxDate: Date;

  dateFromName: string;
  dateToName: string;
  displayDomainCheckBox: boolean;

  matcher: ErrorStateMatcher = showOnDirtyErrorStateMatcher;

  filter: any;

  constructor(private http: Http, private alertService: AlertService, public dialog: MdDialog, private securityService: SecurityService) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this.getAlertTypes();
    this.getAlertLevels();
    this.getAlertStatuses();
    
    this.loading = false;
    this.rows = [];
    this.count = 0;
    this.offset = 0;
    this.isChanged = false;

    this.aTypes = [];
    this.aStatuses = [];
    this.aLevels = [];

    this.dynamicFilters = [];
    this.dynamicDatesFilter = {};
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;

    this.timestampCreationFromMaxDate = new Date();
    this.timestampCreationToMinDate = null;
    this.timestampCreationToMaxDate = new Date();
    this.timestampReportingFromMaxDate = new Date();
    this.timestampReportingToMinDate = null;
    this.timestampReportingToMaxDate = new Date();

    this.dateFromName = '';
    this.dateToName = '';
    this.displayDomainCheckBox = this.securityService.isCurrentUserSuperAdmin();

    this.filter = {processed: 'UNPROCESSED', domainAlerts: false};

    this.columnPicker.allColumns = [
      {name: 'Alert Id', width: 20, prop: 'entityId'},
      {name: 'Processed', cellTemplate: this.rowProcessed, width: 20},
      {name: 'Alert Type'},
      {name: 'Alert Level', width: 50},
      {name: 'Alert Status', width: 50},
      {name: 'Creation Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Reporting Time', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Parameters', cellTemplate: this.rowWithSpaceAfterCommaTpl, sortable: false},
      {name: 'Sent Attempts', width: 50, prop: 'attempts',},
      {name: 'Max Attempts', width: 50},
      {name: 'Next Attempt', cellTemplate: this.rowWithDateFormatTpl, width: 155},
      {name: 'Reporting Time Failure', cellTemplate: this.rowWithDateFormatTpl, width: 155}
    ];
    this.rowLimiter = new RowLimiterBase();

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Processed', 'Alert Type', 'Alert Level', 'Alert Status', 'Creation Time', 'Reporting Time', 'Parameters'].indexOf(col.name) != -1
    });

    this.orderBy = 'creationTime';
    this.asc = false;

    this.search();
  }

  getAlertTypes(): void {
    this.http.get(AlertsComponent.ALERTS_TYPES_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aTypes => this.aTypes = aTypes);
  }

  getAlertStatuses(): void {
    this.http.get(AlertsComponent.ALERTS_STATUS_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aStatuses => this.aStatuses = aStatuses);
  }

  getAlertLevels(): void {
    this.http.get(AlertsComponent.ALERTS_LEVELS_URL)
      .map(this.extractData)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aLevels => this.aLevels = aLevels);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

  getAlertsEntries(offset: number, pageSize: number): Observable<AlertsResult> {
    const searchParams = this.createSearchParams();

    searchParams.set('page', offset.toString());
    searchParams.set('pageSize', pageSize.toString());

    return this.http.get(AlertsComponent.ALERTS_URL, {
      search: searchParams
    }).map((response: Response) =>
      response.json()
    );
  }

  private createSearchParams() {
    const searchParams = this.createStaticSearchParams();

    if (this.dynamicFilters.length > 0) {
      for (let filter of this.dynamicFilters) {
        searchParams.append('parameters', filter || ''); // do not merge this in 4.2, this code doesn't apply there anymore
      }
    }

    if (this.alertTypeWithDate) {
      const from = this.dynamicDatesFilter.from;
      if (from) {
        searchParams.set('dynamicFrom', from.getTime());
      }

      const to = this.dynamicDatesFilter.to;
      if (to) {
        searchParams.set('dynamicTo', to.getTime());
      }
    }
    return searchParams;
  }

  private createStaticSearchParams() {
    const searchParams: URLSearchParams = new URLSearchParams();

    searchParams.set('orderBy', this.orderBy);
    if (this.asc != null) {
      searchParams.set('asc', this.asc.toString());
    }

    // filters
    if (this.activeFilter.processed) {
      searchParams.set('processed', this.activeFilter.processed === 'PROCESSED' ? 'true' : 'false');
    }

    if (this.activeFilter.alertType) {
      searchParams.set('alertType', this.activeFilter.alertType);
    }

    if (this.activeFilter.alertStatus) {
      searchParams.set('alertStatus', this.activeFilter.alertStatus);
    }

    if (this.activeFilter.alertId) {
      searchParams.set('alertId', this.activeFilter.alertId);
    }

    if (this.activeFilter.alertLevel) {
      searchParams.set('alertLevel', this.activeFilter.alertLevel);
    }

    if (this.activeFilter.creationFrom) {
      searchParams.set('creationFrom', this.activeFilter.creationFrom.getTime());
    }

    if (this.activeFilter.creationTo) {
      searchParams.set('creationTo', this.activeFilter.creationTo.getTime());
    }

    if (this.activeFilter.reportingFrom) {
      searchParams.set('reportingFrom', this.activeFilter.reportingFrom.getTime());
    }

    if (this.activeFilter.reportingTo) {
      searchParams.set('reportingTo', this.activeFilter.reportingTo.getTime());
    }

    searchParams.set('domainAlerts', this.activeFilter.domainAlerts);
    return searchParams;
  }

  page(offset, pageSize) {
    this.loading = true;
    this.resetFilters();
    this.getAlertsEntries(offset, pageSize).subscribe((result: AlertsResult) => {
      console.log('alerts response: ' + result);
      this.offset = offset;
      this.rowLimiter.pageSize = pageSize;
      this.count = result.count;
      const start = offset * pageSize;
      const end = start + pageSize;
      const newRows = [...result.alertsEntries];

      let index = 0;
      for (let i = start; i < end; i++) {
        newRows[i] = result.alertsEntries[index++];
      }

      this.rows = newRows;

      this.loading = false;
    }, (error: any) => {
      console.log('error getting the alerts:' + error);
      this.loading = false;
      this.alertService.exception('Error occurred:', error);
    });
  }

  search() {
    this.isChanged = false;
    this.setActiveFilter();
    this.page(0, this.rowLimiter.pageSize);
  }

  toggleAdvancedSearch() {
    this.advancedSearch = !this.advancedSearch;
    return false; // to prevent default navigation
  }

  getAlertParameters(alertType: string): Observable<Array<string>> {
    const searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('alertType', alertType);
    return this.http.get(AlertsComponent.ALERTS_PARAMS_URL, {search: searchParams}).map(this.extractData);
  }

  onAlertTypeChanged(alertType: string) {
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;
    this.dynamicFilters = [];
    this.dynamicDatesFilter = [];
    const alertParametersObservable = this.getAlertParameters(alertType).flatMap(value => value);
    const TIME_SUFFIX = '_TIME';
    const DATE_SUFFIX = '_DATE';
    let nonDateParamerters = alertParametersObservable.filter(value => {
      console.log('Value:' + value);
      return (value.search(TIME_SUFFIX) === -1 && value.search(DATE_SUFFIX) === -1)
    });
    nonDateParamerters.subscribe(item => this.nonDateParameters.push(item));
    let dateParameters = alertParametersObservable.filter(value => {
      return value.search(TIME_SUFFIX) > 0 || value.search(DATE_SUFFIX) > 1
    });
    dateParameters.subscribe(item => {
      this.dateFromName = item + ' FROM';
      this.dateToName = item + ' TO';
      this.alertTypeWithDate = true;
    });
  }

  onTimestampCreationFromChange(event) {
    this.timestampCreationToMinDate = event.value;
  }

  onTimestampCreationToChange(event) {
    this.timestampCreationFromMaxDate = event.value;
  }

  onTimestampReportingFromChange(event) {
    this.timestampReportingToMinDate = event.value;
  }

  onTimestampReportingToChange(event) {
    this.timestampReportingFromMaxDate = event.value;
  }


  // datatable methods

  onPage(event) {
    this.page(event.offset, event.pageSize);
  }

  /**
   * The method is an override of the abstract method defined in SortableList mixin
   */
  public reload() {
    this.page(0, this.rowLimiter.pageSize);
  }

  changePageSize(newPageLimit: number) {
    this.rowLimiter.pageSize = newPageLimit;
    this.page(0, newPageLimit);
  }

  saveAsCSV() {
    if (this.isChanged) {
      this.save(true);
    } else {
      if (this.count > AlertComponent.MAX_COUNT_CSV) {
        this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
        return;
      }

      super.resetFilters();
      // todo: add dynamic params for csv filtering, if requested
      DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL + '?'
        + this.createSearchParams().toString());
    }
  }

  cancel() {
    this.dialog.open(CancelDialogComponent)
      .afterClosed().subscribe(result => {
      if (result) {
        this.isChanged = false;
        this.page(this.offset, this.rowLimiter.pageSize);
      }
    });
  }

  save(withDownloadCSV: boolean) {
    const dialogRef = this.dialog.open(SaveDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.http.put(AlertsComponent.ALERTS_URL, this.rows).subscribe(() => {
          this.alertService.success('The operation \'update alerts\' completed successfully.', false);
          this.page(this.offset, this.rowLimiter.pageSize);
          this.isChanged = false;
          if (withDownloadCSV) {
            DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL);
          }
        }, err => {
          this.alertService.exception('The operation \'update alerts\' not completed successfully', err);
          this.page(this.offset, this.rowLimiter.pageSize);
        });
      } else {
        if (withDownloadCSV) {
          DownloadService.downloadNative(AlertsComponent.ALERTS_CSV_URL);
        }
      }
    });
  }

  setProcessedValue(row) {
    this.isChanged = true;
    row.processed = !row.processed;
    this.rows[row.$$index] = row;
  }

  isDirty(): boolean {
    return this.isChanged;
  }
}
