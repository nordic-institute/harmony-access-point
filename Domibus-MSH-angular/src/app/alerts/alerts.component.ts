import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertsResult} from './alertsresult';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {ErrorStateMatcher, MatDialog, ShowOnDirtyErrorStateMatcher} from '@angular/material';
import {SecurityService} from '../security/security.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import SortableListMixin from '../common/mixins/sortable-list.mixin';
import {DirtyOperations} from '../common/dirty-operations';
import 'rxjs-compat/add/operator/filter';
import {DialogsService} from '../common/dialogs/dialogs.service';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';

@Component({
  moduleId: module.id,
  templateUrl: 'alerts.component.html',
  providers: []
})

export class AlertsComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, SortableListMixin, ModifiableListMixin, ServerPageableListMixin)
  implements OnInit {

  static readonly ALERTS_URL: string = 'rest/alerts';
  static readonly ALERTS_CSV_URL: string = AlertsComponent.ALERTS_URL + '/csv';
  static readonly ALERTS_TYPES_URL: string = AlertsComponent.ALERTS_URL + '/types';
  static readonly ALERTS_STATUS_URL: string = AlertsComponent.ALERTS_URL + '/status';
  static readonly ALERTS_LEVELS_URL: string = AlertsComponent.ALERTS_URL + '/levels';
  static readonly ALERTS_PARAMS_URL: string = AlertsComponent.ALERTS_URL + '/params';

  @ViewChild('rowProcessed', {static: false}) rowProcessed: TemplateRef<any>;
  @ViewChild('rowWithDateFormatTpl', {static: false}) public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rowWithSpaceAfterCommaTpl', {static: false}) public rowWithSpaceAfterCommaTpl: TemplateRef<any>;

  advancedSearch: boolean;

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

    super.filter = {processed: 'UNPROCESSED', domainAlerts: false};

    super.orderBy = 'creationTime';
    super.asc = false;

    super.setActiveFilter();
    this.search();
  }

  public get name(): string {
    return 'Alerts';
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
      return ['Processed', 'Alert Type', 'Alert Level', 'Alert Status', 'Creation Time', 'Reporting Time'].indexOf(col.name) != -1
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

  getAlertsEntries(): Promise<AlertsResult> {
    let searchParams = this.createSearchParams();

    searchParams = searchParams.append('page', this.offset.toString());
    searchParams = searchParams.append('pageSize', this.rowLimiter.pageSize.toString());

    return this.http.get<AlertsResult>(AlertsComponent.ALERTS_URL, {params: searchParams}).toPromise();
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

  async doLoadPage(): Promise<any> {
    return this.getAlertsEntries().then((result: AlertsResult) => {
      super.count = result.count;
      super.rows = result.alertsEntries;
    });
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

  async doSave(): Promise<any> {
    return this.http.put(AlertsComponent.ALERTS_URL, this.rows).toPromise();
  }

  setProcessedValue(row) {
    super.isChanged = true;
  }

  public get csvUrl(): string {
    // todo: add dynamic params for csv filtering, if requested
    return AlertsComponent.ALERTS_CSV_URL + '?' + this.createSearchParams().toString();
  }
}
