import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import {Observable} from 'rxjs/Observable';
import {AlertsResult} from './alertsresult';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {ErrorStateMatcher, MatDialog, ShowOnDirtyErrorStateMatcher} from '@angular/material';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import SortableListMixin from '../common/mixins/sortable-list.mixin';
import {DirtyOperations} from '../common/dirty-operations';
import {AlertsEntry} from './alertsentry';
import 'rxjs-compat/add/operator/filter';
import {DialogsService} from '../common/dialogs/dialogs.service';

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

  @ViewChild('rowProcessed', {static: false}) rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl', {static: false}) public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithSpaceAfterCommaTpl', {static: false}) public rowWithSpaceAfterCommaTpl: TemplateRef<any>;

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

  matcher: ErrorStateMatcher = new ShowOnDirtyErrorStateMatcher;

  filter: any;

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  constructor(private http: HttpClient, private alertService: AlertService, public dialog: MatDialog,
              private dialogsService: DialogsService,
              private securityService: SecurityService, private changeDetector: ChangeDetectorRef) {
    super();

    this.getAlertTypes();
    this.getAlertLevels();
    this.getAlertStatuses();
  }

  ngOnInit() {
    super.ngOnInit();

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

    this.rowLimiter = new RowLimiterBase();

    this['orderBy'] = 'creationTime';
    this['asc'] = false;

    this.search();
  }

  ngAfterViewInit() {
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

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Processed', 'Alert Type', 'Alert Level', 'Alert Status', 'Creation Time', 'Reporting Time', 'Parameters'].indexOf(col.name) != -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  getAlertTypes(): void {
    this.http.get<any[]>(AlertsComponent.ALERTS_TYPES_URL)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aTypes => this.aTypes = aTypes);
  }

  getAlertStatuses(): void {
    this.http.get<any[]>(AlertsComponent.ALERTS_STATUS_URL)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aStatuses => this.aStatuses = aStatuses);
  }

  getAlertLevels(): void {
    this.http.get<any[]>(AlertsComponent.ALERTS_LEVELS_URL)
      .catch(err => this.alertService.handleError(err))
      .subscribe(aLevels => this.aLevels = aLevels);
  }

  getAlertsEntries(offset: number, pageSize: number): Observable<AlertsResult> {
    let searchParams = this.createSearchParams();

    searchParams = searchParams.append('page', offset.toString());
    searchParams = searchParams.append('pageSize', pageSize.toString());

    return this.http.get<AlertsResult>(AlertsComponent.ALERTS_URL, {params: searchParams});
  }

  private createSearchParams() {
    let searchParams = this.createStaticSearchParams();

    if (this.dynamicFilters.length > 0) {
      for (let filter of this.dynamicFilters) {
        searchParams = searchParams.append('parameters', filter || '');
      }
    }

    if (this.alertTypeWithDate) {
      const from = this.dynamicDatesFilter.from;
      if (from) {
        searchParams = searchParams.append('dynamicFrom', from.getTime());
      }

      const to = this.dynamicDatesFilter.to;
      if (to) {
        searchParams = searchParams.append('dynamicTo', to.getTime());
      }
    }
    return searchParams;
  }

  private createStaticSearchParams() {
    let searchParams = new HttpParams();

    searchParams = searchParams.append('orderBy', this.orderBy);
    if (this.asc != null) {
      searchParams = searchParams.append('asc', this.asc.toString());
    }

    // filters
    if (this.activeFilter.processed) {
      searchParams = searchParams.append('processed', this.activeFilter.processed === 'PROCESSED' ? 'true' : 'false');
    }

    if (this.activeFilter.alertType) {
      searchParams = searchParams.append('alertType', this.activeFilter.alertType);
    }

    if (this.activeFilter.alertStatus) {
      searchParams = searchParams.append('alertStatus', this.activeFilter.alertStatus);
    }

    if (this.activeFilter.alertId) {
      searchParams = searchParams.append('alertId', this.activeFilter.alertId);
    }

    if (this.activeFilter.alertLevel) {
      searchParams = searchParams.append('alertLevel', this.activeFilter.alertLevel);
    }

    if (this.activeFilter.creationFrom) {
      searchParams = searchParams.append('creationFrom', this.activeFilter.creationFrom.getTime());
    }

    if (this.activeFilter.creationTo) {
      searchParams = searchParams.append('creationTo', this.activeFilter.creationTo.getTime());
    }

    if (this.activeFilter.reportingFrom) {
      searchParams = searchParams.append('reportingFrom', this.activeFilter.reportingFrom.getTime());
    }

    if (this.activeFilter.reportingTo) {
      searchParams = searchParams.append('reportingTo', this.activeFilter.reportingTo.getTime());
    }

    searchParams = searchParams.append('domainAlerts', this.activeFilter.domainAlerts);
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

  getAlertParameters(alertType: string): Promise<string[]> {
    let searchParams = new HttpParams();
    searchParams = searchParams.append('alertType', alertType);
    return this.http.get<string[]>(AlertsComponent.ALERTS_PARAMS_URL, {params: searchParams}).toPromise();
  }

  async onAlertTypeChanged(alertType: string) {
    this.nonDateParameters = [];
    this.alertTypeWithDate = false;
    this.dynamicFilters = [];
    this.dynamicDatesFilter = [];
    const alertParameters = await this.getAlertParameters(alertType);
    const TIME_SUFFIX = '_TIME';
    const DATE_SUFFIX = '_DATE';
    let nonDateParameters = alertParameters.filter((value) => {
      console.log('Value:' + value);
      return (value.search(TIME_SUFFIX) === -1 && value.search(DATE_SUFFIX) === -1)
    });
    this.nonDateParameters.push(...nonDateParameters);
    let dateParameters = alertParameters.filter((value) => {
      return value.search(TIME_SUFFIX) > 0 || value.search(DATE_SUFFIX) > 1
    });
    dateParameters.forEach(item => {
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
    this.dialogsService.openCancelDialog().then(cancel => {
      if (cancel) {
        this.isChanged = false;
        this.page(this.offset, this.rowLimiter.pageSize);
      }
    });
  }

  save(withDownloadCSV: boolean) {
    this.dialogsService.openSaveDialog().then(save => {
      if (save) {
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
    this.rows[this.rows.indexOf(row)] = row;
  }

  isDirty(): boolean {
    return this.isChanged;
  }
}
