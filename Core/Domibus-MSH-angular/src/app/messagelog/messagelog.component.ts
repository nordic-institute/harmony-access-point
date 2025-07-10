import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MessageLogResult} from './support/messagelogresult';
import {AlertService} from '../common/alert/alert.service';
import {MatSelectChange} from '@angular/material/select';
import {MessagelogDetailsComponent} from 'app/messagelog/messagelog-details/messagelog-details.component';
import {DownloadService} from '../common/download.service';
import {DatatableComponent} from '@swimlane/ngx-datatable';
import {DomibusInfoService} from '../common/appinfo/domibusinfo.service';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {ServerSortableListMixin} from '../common/mixins/sortable-list.mixin';
import BaseListComponent from '../common/mixins/base-list.component';
import mix from '../common/mixins/mixin.utils';
import {DialogsService} from '../common/dialogs/dialogs.service';
import {ServerPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';
import {PropertiesService} from '../properties/support/properties.service';
import * as moment from 'moment';
import {Moment} from 'moment';
import 'moment-precise-range-plugin';
import {SecurityService} from '../security/security.service';
import {ComponentName} from '../common/component-name-decorator';
import {MessageLogEntry} from './support/messagelogentry';
import {FormControl, NgForm} from '@angular/forms';
import 'rxjs/add/observable/interval';
import {Observable} from 'rxjs/Observable';
import {Subscription} from 'rxjs';
import {DateService} from '../common/customDate/date.service';

@Component({
  templateUrl: 'messagelog.component.html',
  providers: [],
  styleUrls: ['./messagelog.component.css']
})
@ComponentName('Message Logs')
export class MessageLogComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin, ServerSortableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked, OnDestroy {

  static readonly RESEND_URL: string = 'rest/message/restore?messageId=${messageId}';
  static readonly RESEND_SELECTED_URL: string = 'rest/message/failed/restore/selected';
  static readonly RESEND_ALL_URL: string = 'rest/message/failed/restore/filtered';
  static readonly DOWNLOAD_MESSAGE_URL: string = 'rest/message/download?messageId=${messageId}&mshRole=${mshRole}';
  static readonly CAN_DOWNLOAD_MESSAGE_URL: string = 'rest/message/exists?messageId=${messageId}&mshRole=${mshRole}';
  static readonly MESSAGE_LOG_URL: string = 'rest/messagelog';
  static readonly DOWNLOAD_ENVELOPE_URL: string = 'rest/message/envelopes?messageId=${messageId}&mshRole=${mshRole}';

  @ViewChild('rowWithDateFormatTpl') public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoTpl') public nextAttemptInfoTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoWithDateFormatTpl') public nextAttemptInfoWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rawTextTpl') public rawTextTpl: TemplateRef<any>;
  @ViewChild('rowActions') rowActions: TemplateRef<any>;
  @ViewChild('list') list: DatatableComponent;
  @ViewChild('receivedToField') receivedToField: FormControl;
  @ViewChild('filterForm') filterForm: NgForm;

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  mshRoles: Array<String>;
  msgTypes: Array<String>;
  msgStatuses: Array<String>;
  notifStatus: Array<String>;

  messageResent: EventEmitter<boolean>;

  searchUserMessages: boolean;
  conversationIdValue: String;
  notificationStatusValue: String;

  resendReceivedMinutes: number;

  additionalPages: number;
  totalRowsMessage: string;
  estimatedCount: boolean;
  resendingAll: boolean;

  messageIntervals = [
    {value: 30, text: 'Last 30 minutes'},
    {value: 60, text: 'Last hour'},
    {value: 4 * 60, text: 'Last 4 hours'},
    {value: 12 * 60, text: 'Last 12 hours'},
    {value: 24 * 60, text: 'Last 24 hours'},
    {value: 2 * 24 * 60, text: 'Last 48 hours'},
    {value: 3 * 24 * 60, text: 'Last 3 days'},
    {value: 7 * 24 * 60, text: 'Last 7 days'},
    {value: 30 * 24 * 60, text: 'Last 30 days'},
    {value: 182.5 * 24 * 60, text: 'Last 6 months'},
    {value: 365 * 24 * 60, text: 'Last year'},
    {value: 5 * 365 * 24 * 60, text: 'Last 5 years'},
    {value: 0, text: 'Custom'},
  ];

  MS_PER_MINUTE = 60000;
  _messageInterval: DateInterval;
  detailedSearch: boolean;
  detailedSearchFields = ['originalSender', 'finalRecipient', 'action', 'serviceType', 'serviceValue'];
  sortedColumns: [{ prop: string; dir: string }];
  receivedToFieldSub: Subscription;

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              private domibusInfoService: DomibusInfoService, public dialogsService: DialogsService, private dateService: DateService,
              private elementRef: ElementRef, private changeDetector: ChangeDetectorRef, private propertiesService: PropertiesService,
              private securityService: SecurityService) {
    super();
    this.receivedToFieldSub = Observable.interval(2000)
      .subscribe((val) => {
        if (this.receivedToField && this.receivedToField.errors && this.receivedToField.errors['matDatetimePickerMax']) {
          const diff = this.filter.receivedTo.getTime() - this.timestampToMaxDate.getTime();
          // console.log('fixing receivedToField=', diff)
          if (diff < 100000) {
            this.filterForm.controls['receivedTo'].setErrors(null);
            this.timestampToMaxDate = new Date(this.filter.receivedTo.getTime() + 60000);
          }
        }
      });
  }

  async ngOnInit() {
    super.ngOnInit();

    this.detailedSearch = await this.isMessageLogPageAdvancedSearchEnabled();
    console.log('detailedSearch=', this.detailedSearch)

    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

    this.additionalPages = 0;
    this.totalRowsMessage = '$1 total';
    this.estimatedCount = false;

    this.messageResent = new EventEmitter(false);

    this.searchUserMessages = true;

    if (this.isCurrentUserAdmin()) {
      this.resendReceivedMinutes = await this.getResendButtonEnabledReceivedMinutes();
    }

    super.filter = {testMessage: false};
    this.messageInterval = await this.getMessageLogInitialInterval();
    this.applyDetailSearchLogic();

    this.filterData();
  }

  get messageInterval(): DateInterval {
    return this._messageInterval;
  }

  set messageInterval(dateInterval: DateInterval) {
    if (this._messageInterval == dateInterval) {
      return;
    }
    this._messageInterval = dateInterval;
    if (dateInterval.value) {
      this.setDatesFromInterval();
    } else {
      this.filter.receivedFrom = null;
      this.filter.receivedTo = null;
      super.advancedSearch = true;
    }
  }

  private setDatesFromInterval() {
    if (this.messageInterval && this.messageInterval.value) {
      this.timestampToMaxDate = this.dateService.todayEndDay();
      this.timestampFromMaxDate = this.dateService.todayEndDay();

      let now = new Date();
      this.filter.receivedTo = now;

      let receivedFrom = new Date(now.getTime() - this.messageInterval.value * this.MS_PER_MINUTE);
      this.filter.receivedFrom = receivedFrom;
      this.timestampToMinDate = receivedFrom;
    }
  }

  private async getMessageLogInitialInterval(): Promise<DateInterval> {
    const res = await this.propertiesService.getMessageLogInitialIntervalProperty();
    const val = +res.value;
    let interval = this.messageIntervals.find(el => el.value == val * 60);
    if (interval) {
      return interval;
    }

    const newValue = this.addInterval(val);
    return newValue;
  }

  private addInterval(val: number) {
    const starts = moment().subtract(val, 'hours');
    const ends = moment();
    // @ts-ignore
    const diffHuman = moment.preciseDiff(starts, ends);
    const newValue = {value: val * 60, text: 'Last ' + diffHuman};

    let index = this.messageIntervals.findIndex(el => el.value > val * 60);
    if (index < 0) {
      index = this.messageIntervals.length - 1;
    }
    this.messageIntervals.splice(index, 0, newValue);
    return newValue;
  }

  async ngAfterViewInit() {
    this.configureColumnPicker();
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private configureColumnPicker() {
    this.columnPicker.allColumns = [
      {
        name: 'Message Id',
        cellTemplate: this.rawTextTpl,
        width: 300,
        minWidth: 290
      },
      {
        name: 'From Party Id',
        width: 200,
        minWidth: 190
      },
      {
        name: 'To Party Id',
        width: 200,
        minWidth: 190
      },
      {
        name: 'Message Status',
        width: 200,
        minWidth: 190
      },
      {
        name: 'Notification Status',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Received',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Downloaded',
        width: 200,
        minWidth: 190
      },
      {
        name: 'AP Role',
        prop: 'mshRole',
        width: 150,
        minWidth: 140
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts',
        width: 70,
        minWidth: 70
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts Max',
        width: 70,
        minWidth: 70
      },
      {
        cellTemplate: this.nextAttemptInfoWithDateFormatTpl,
        name: 'Next Attempt',
        width: 200,
        minWidth: 190
      },
      {
        name: 'Conversation Id',
        cellTemplate: this.rawTextTpl,
        width: 300,
        minWidth: 290
      },
      {
        name: 'Message Type',
        width: 170,
        minWidth: 160
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Deleted',
        width: 200,
        minWidth: 190
      },
    ];

    this.columnPicker.allColumns.push(
      {
        name: 'Action',
        prop: 'action',
        disabled: () => !this.detailedSearch,
        width: 100,
        minWidth: 90
      },
      {
        name: 'Service Type',
        prop: 'serviceType',
        disabled: () => !this.detailedSearch,
        width: 100,
        minWidth: 90
      },
      {
        name: 'Service Value',
        prop: 'serviceValue',
        disabled: () => !this.detailedSearch,
        width: 100,
        minWidth: 90
      });

    this.columnPicker.allColumns.push(
      {
        name: 'Original Sender',
        prop: 'originalSender',
        cellTemplate: this.rawTextTpl,
        disabled: () => !this.detailedSearch,
        width: 100,
        minWidth: 90
      },
      {
        name: 'Final Recipient',
        prop: 'finalRecipient',
        cellTemplate: this.rawTextTpl,
        disabled: () => !this.detailedSearch,
        width: 100,
        minWidth: 90
      });

    this.columnPicker.allColumns.push(
      {
        name: 'Ref To Message Id',
        cellTemplate: this.rawTextTpl,
        width: 100,
        minWidth: 90
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Acknowledged',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Failed',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Restored',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Exported',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Archived',
        width: 200,
        minWidth: 190
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 180,
        minWidth: 180,
        sortable: false
      }
    );

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['Message Id', 'From Party Id', 'To Party Id', 'Message Status', 'Received', 'AP Role', 'Message Type', 'Actions'].indexOf(col.name) != -1
    });
  }

  public beforeDomainChange() {
    if (this.list.isHorScroll) {
      this.scrollLeft();
    }
  }

  protected get GETUrl(): string {
    return MessageLogComponent.MESSAGE_LOG_URL;
  }

  protected createAndSetParameters(): HttpParams {
    let filterParams = super.createAndSetParameters();
    this.columnPicker.allColumns
      .filter(col => col.isSelected)
      .forEach(col => filterParams = filterParams.append('fields', col.prop));

    console.log('filterParams==', filterParams);
    return filterParams;
  }

  public setServerResults(result: MessageLogResult) {
    this.calculateCount(result);
    super.rows = result.messageLogEntries;

    if (result.filter.receivedFrom) {
      result.filter.receivedFrom = new Date(result.filter.receivedFrom);
    }
    if (result.filter.receivedTo) {
      result.filter.receivedTo = new Date(result.filter.receivedTo);
    }
    this.syncInterval(result.filter);

    super.filter = result.filter;

    this.mshRoles = result.mshRoles;
    this.msgTypes = result.msgTypes;
    this.msgStatuses = result.msgStatus.sort();
    this.notifStatus = result.notifStatus;
  }

  private syncInterval(filter: any) {
    if (filter.receivedFrom && filter.receivedTo) {
      const diff = (filter.receivedTo.valueOf() - filter.receivedFrom.valueOf()) / this.MS_PER_MINUTE;
      this._messageInterval = this.messageIntervals.find(el => el.value == diff) || this.messageIntervals[this.messageIntervals.length - 1];
    }
  }

  protected onBeforeFilter() {
    this.setDatesFromInterval();
  }

  protected onSetFilters() {
    this.syncInterval(this.filter);
  }

  private calculateCount(result: MessageLogResult) {
    this.estimatedCount = result.estimatedCount;
    if (result.estimatedCount) {
      if (result.messageLogEntries.length < this.rowLimiter.pageSize) {
        this.additionalPages--;
      }
      super.count = result.count + this.additionalPages * this.rowLimiter.pageSize;
      this.totalRowsMessage = 'more than $1';
    } else {
      super.count = result.count;
      this.totalRowsMessage = '$1 total';
    }
  }

  public async onPage(event) {
    if (this.estimatedCount && ((event.offset + 1) * this.rowLimiter.pageSize > this.count)) {
      this.additionalPages++;
    }
    super.onPage(event);
  }

  resendDialog() {
    this.dialogsService.openResendDialog().then(resend => {
      if (resend && this.selected[0]) {
        this.resend(this.selected[0].messageId);
        super.selected = [];
        this.messageResent.subscribe(() => {
          this.page();
        });
      }
    });
  }

  async resendAllDialog() {
    if (this.resendingAll) {
      this.alertService.error('Resending is already in progress. Please wait for it to finish.');
      return;
    }
    const resend = await this.dialogsService.openResendAllDialog();
    if (!resend) {
      return;
    }
    this.resendAll();
    super.selected = [];
    this.messageResent.subscribe(() => {
      this.page();
    });
  }

  async resendSelectedDialog() {
    const resendSelected = await this.dialogsService.openResendSelectedDialog();
    if (!resendSelected) {
      return;
    }
    this.resendSelected(this.selected);
    super.selected = [];
    this.messageResent.subscribe(() => {
      this.page();
    });
  }

  resend(messageId: string) {
    console.log('Resending message with id ', messageId);

    let url = MessageLogComponent.RESEND_URL.replace('${messageId}', encodeURIComponent(messageId));

    this.http.put(url, {}, {}).subscribe(res => {
      this.alertService.success('The operation resend message completed successfully');
      window.setTimeout(() => {
        this.messageResent.emit();
      }, 500);
    }, err => {
      this.alertService.exception('The message ' + this.alertService.escapeHtml(messageId) + ' could not be resent.', err);
    });
  }

  resendAll() {
    this.resendingAll = true;
    const filters = this.getFiltersAsObject();
    let url = MessageLogComponent.RESEND_ALL_URL;
    this.http.put(url, filters).subscribe(res => {
      this.alertService.success('The operation resend messages scheduled successfully. Please refresh the page after sometime.');
      window.setTimeout(() => {
        this.messageResent.emit();
      }, 500);
      this.resendingAll = false;
    }, err => {
      this.alertService.exception('The messages could not be resent.', err);
      this.resendingAll = false;
    });
  }

  resendSelected(messageLogEntries: MessageLogEntry[]) {
    let url = MessageLogComponent.RESEND_SELECTED_URL;
    let messageIds = messageLogEntries.map(entry => ({messageId: entry.messageId, mshRole: entry.mshRole, messageStatus: entry.messageStatus}));
    this.http.put(url, messageIds).subscribe(res => {
      this.alertService.success('The operation resend messages completed successfully');
      window.setTimeout(() => {
        this.messageResent.emit();
      }, 500);
    }, err => {
      this.alertService.exception('The messages could not be resent.', err);
    });
  }

  isResendButtonEnabled() {
    return this.isOneRowSelected() && !this.selected[0].deleted
      && this.isRowResendButtonEnabled(this.selected[0]);
  }

  isResendButtonEnabledAction(row): boolean {
    return this.isRowResendButtonEnabled(row);
  }

  private isRowResendButtonEnabled(row): boolean {
    return !row.deleted
      && (row.messageStatus === 'SEND_FAILURE' || this.isResendButtonEnabledForSendEnqueued(row))
      && !this.isSplitAndJoinMessage(row);
  }

  isResendAllButtonEnabled() {
    return !this.resendingAll && this.rows.length > 1 && this.isMoreRowsWithSendFailure()
      && this.rows.filter(row => this.isRowResendButtonEnabled(row)).length > 1;
  }

  isResendSelectedButtonEnabled() {
    return this.isMoreRowsSelectedWithSendFailure() && this.selected.filter(row => this.isRowResendButtonEnabled(row)).length > 1;
  }


  private isResendButtonEnabledForSendEnqueued(row): boolean {
    let receivedDateDelta = moment(row.received).add(this.resendReceivedMinutes, 'minutes');

    return (row.messageStatus === 'SEND_ENQUEUED' && receivedDateDelta.isBefore(new Date()) && !row.nextAttempt)
  }

  private async getResendButtonEnabledReceivedMinutes(): Promise<number> {
    const res = await this.propertiesService.getResendButtonEnabledReceivedMinutesProperty();
    return +res.value;
  }

  private isSplitAndJoinMessage(row) {
    return row.messageFragment || row.sourceMessage;
  }

  isDownloadButtonEnabled(): boolean {
    return this.isOneRowSelected() && this.selected[0].canDownloadMessage;
  }

  private isOneRowSelected() {
    return this.selected && this.selected.length == 1;
  }

  private isMoreRowsSelectedWithSendFailure() {
    return this.selected && this.selected.length > 1
      && this.isMoreSelectedWithSendFailure();
  }

  private isMoreSelectedWithSendFailure() {
    return this.selected.filter(row => row.messageStatus === 'SEND_FAILURE').length > 1;
  }

  private isMoreRowsWithSendFailure() {
    return this.rows.filter(row => row.messageStatus === 'SEND_FAILURE').length > 1;
  }

  private async downloadMessage(row) {
    const messageId = row.messageId;
    const mshRole = row.mshRole;
    let canDownloadUrl = MessageLogComponent.CAN_DOWNLOAD_MESSAGE_URL
      .replace('${messageId}', encodeURIComponent(messageId))
      .replace('${mshRole}', mshRole);
    this.http.get(canDownloadUrl).subscribe(res => {
      const downloadUrl = MessageLogComponent.DOWNLOAD_MESSAGE_URL
        .replace('${messageId}', encodeURIComponent(messageId))
        .replace('${mshRole}', mshRole);
      DownloadService.downloadNative(downloadUrl);
    }, err => {
      if (err.error.message.includes('Message content is no longer available for message id')) {
        row.deleted = true;
      }
      this.alertService.exception(`Could not download message.`, err);
    });
  }

  downloadEnvelopeAction(row: MessageLogEntry) {
    if (row.messageType == 'USER_MESSAGE') {
      this.downloadEnvelopesForUserMessage(row.messageId, row.mshRole);
    } else {
      this.downloadEnvelopesForSignalMessage(row, row.mshRole);
    }
  }

  private downloadEnvelopesForSignalMessage(row, mshRole) {
    this.downloadEnvelopesForUserMessage(row.refToMessageId, mshRole);
  }

  private async downloadEnvelopesForUserMessage(messageId, mshRole) {
    try {
      const downloadUrl = MessageLogComponent.DOWNLOAD_ENVELOPE_URL
        .replace('${messageId}', encodeURIComponent(messageId))
        .replace('${mshRole}', mshRole);
      const res = await this.http.get(downloadUrl, {responseType: 'arraybuffer' as 'json'}).toPromise();
      if (!res) {
        this.alertService.error('Could not find envelopes to download.');
        return;
      }
      DownloadService.downloadNative(downloadUrl);
    } catch (err) {
      this.alertService.exception(`Could not download message envelopes for id ${messageId}.`, err);
    }
  }

  downloadAction(row) {
    this.downloadMessage(row);
  }

  download() {
    this.downloadMessage(this.selected[0]);
  }

  get csvUrl(): string {
    return MessageLogComponent.MESSAGE_LOG_URL + '/csv?' + this.createAndSetParameters();
  }

  showDetails(selectedRow: any) {
    if (!selectedRow) return;
    let allColumns = <any[]>this.columnPicker.allColumns;
    const allFields = allColumns.map(col => col.prop);
    let fetchData = allColumns.some(col => !col.isSelected);
    this.dialogsService.open(MessagelogDetailsComponent, {
      data: {message: selectedRow, fields: allFields, fetchData}
    });
  }

  onResetAdvancedSearchParams() {
    this.filter.messageType = this.msgTypes[1];
    this.conversationIdValue = null;
    this.notificationStatusValue = null;
    this.setCustomMessageInterval();
  }

  onTimestampFromChange(param: Moment) {
    if (param) {
      this.timestampToMinDate = param.toDate();
      this.filter.receivedFrom = param.toDate();
    } else {
      this.timestampToMinDate = null;
      this.filter.receivedFrom = null;
    }
    this.setCustomMessageInterval();
  }

  onTimestampToChange(param: Moment) {
    if (param) {
      let date = param.toDate();
      this.dateService.setEndDay(date);
      this.timestampFromMaxDate = date;
      this.filter.receivedTo = date;
    } else {
      this.filter.receivedTo = null;
      this.timestampFromMaxDate = this.dateService.todayEndDay();
    }
    this.setCustomMessageInterval();
  }

  private setCustomMessageInterval() {
    this._messageInterval = this.messageIntervals[this.messageIntervals.length - 1];
  }

  private showNextAttemptInfo(row: any): boolean {
    if (row && (row.messageType === 'SIGNAL_MESSAGE' || row.mshRole === 'RECEIVING')) {
      return false;
    }
    return true;
  }

  public scrollLeft() {
    const dataTableBodyDom = this.elementRef.nativeElement.querySelector('.datatable-body');
    dataTableBodyDom.scrollLeft = 0;
  }

  onMessageTypeChanged($event: MatSelectChange) {
    this.searchUserMessages = (this.filter.messageType == 'USER_MESSAGE');
    if (this.searchUserMessages) {
      this.filter.conversationId = this.conversationIdValue;
      this.filter.notificationStatus = this.notificationStatusValue;
    } else {
      this.conversationIdValue = this.filter.conversationId;
      this.filter.conversationId = null;

      this.notificationStatusValue = this.filter.notificationStatus;
      this.filter.notificationStatus = null;
    }
  }

  isCurrentUserAdmin(): boolean {
    return this.securityService.isCurrentUserAdmin();
  }

  protected onAfterResetFilters() {
  }

  async detailedSearchChanged() {
    this.applyDetailSearchLogic();

    const prop = await this.propertiesService.getMessageLogPageAdvancedSearchEnabledProperty();
    prop.value = String(this.detailedSearch);
    this.propertiesService.updateProperty(prop);
  }

  private applyDetailSearchLogic() {
    if (!this.detailedSearch) {
      this.detailedSearchFields.forEach(field => {
        this.filter[field] = null;

        let selectedColumns = this.columnPicker.selectedColumns;
        const col = selectedColumns.find(el => el.prop == field);
        if (col) {
          col.isSelected = false;
        }
      });
    }

    let detailedDefaultSortColumn = 'received';
    super.orderBy = this.detailedSearch ? detailedDefaultSortColumn : 'entityId';
    super.asc = false;
    this.sortedColumns = this.detailedSearch ? this.sortedColumns = [{prop: detailedDefaultSortColumn, dir: 'desc'}] : null;
    console.log('this.sortedColumns =', this.sortedColumns)
    this.columnPicker.allColumns.forEach(col => col.sortable = this.detailedSearch);
  }

  private async isMessageLogPageAdvancedSearchEnabled(): Promise<boolean> {
    const prop = await this.propertiesService.getMessageLogPageAdvancedSearchEnabledProperty();
    return prop && prop.value && prop.value.toLowerCase() == 'true';
  }

  ngOnDestroy() {
    this.receivedToFieldSub.unsubscribe();
  }
}

interface DateInterval {
  value: number
  text: string
}
