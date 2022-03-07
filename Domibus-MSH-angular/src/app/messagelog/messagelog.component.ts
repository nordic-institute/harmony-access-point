import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MessageLogResult} from './support/messagelogresult';
import {AlertService} from '../common/alert/alert.service';
import {MatDialog, MatSelectChange} from '@angular/material';
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
import {SecurityService} from '../security/security.service';
import {ComponentName} from '../common/component-name-decorator';
import {MessageLogEntry} from './support/messagelogentry';

@Component({
  moduleId: module.id,
  templateUrl: 'messagelog.component.html',
  providers: [],
  styleUrls: ['./messagelog.component.css']
})
@ComponentName('Message Logs')
export class MessageLogComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ServerPageableListMixin, ServerSortableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  static readonly RESEND_URL: string = 'rest/message/restore?messageId=${messageId}';
  static readonly DOWNLOAD_MESSAGE_URL: string = 'rest/message/download?messageId=${messageId}';
  static readonly CAN_DOWNLOAD_MESSAGE_URL: string = 'rest/message/exists?messageId=${messageId}';
  static readonly MESSAGE_LOG_URL: string = 'rest/messagelog';
  static readonly DOWNLOAD_ENVELOPE_URL: string = 'rest/message/envelopes?messageId=${messageId}';

  @ViewChild('rowWithDateFormatTpl', {static: false}) public rowWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoTpl', {static: false}) public nextAttemptInfoTpl: TemplateRef<any>;
  @ViewChild('nextAttemptInfoWithDateFormatTpl', {static: false}) public nextAttemptInfoWithDateFormatTpl: TemplateRef<any>;
  @ViewChild('rawTextTpl', {static: false}) public rawTextTpl: TemplateRef<any>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('list', {static: false}) list: DatatableComponent;

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  mshRoles: Array<String>;
  msgTypes: Array<String>;
  msgStatuses: Array<String>;
  notifStatus: Array<String>;

  fourCornerEnabled: boolean;

  messageResent: EventEmitter<boolean>;

  searchUserMessages: boolean;
  conversationIdValue: String;
  notificationStatusValue: String;

  resendReceivedMinutes: number;

  additionalPages: number;
  totalRowsMessage: string;
  estimatedCount: boolean;

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
    {value: 0, text: 'Custom'},
  ];

  MS_PER_MINUTE = 60000;
  _messageInterval: DateInterval;
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
      this.filter.receivedTo = new Date();
      this.filter.receivedFrom = new Date(this.filter.receivedTo - this.messageInterval.value * this.MS_PER_MINUTE);
    }
  }

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              private domibusInfoService: DomibusInfoService, public dialog: MatDialog, public dialogsService: DialogsService,
              private elementRef: ElementRef, private changeDetector: ChangeDetectorRef, private propertiesService: PropertiesService,
              private securityService: SecurityService) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

    super.orderBy = 'received';
    super.asc = false;

    this.additionalPages = 0;
    this.totalRowsMessage = '$1 total';
    this.estimatedCount = false;

    this.messageResent = new EventEmitter(false);

    this.searchUserMessages = true;

    this.fourCornerEnabled = await this.domibusInfoService.isFourCornerEnabled();

    if (this.isCurrentUserAdmin()) {
      this.resendReceivedMinutes = await this.getResendButtonEnabledReceivedMinutes();
    }

    super.filter = {testMessage: false};
    this.messageInterval = this.messageIntervals[1];
    this.filterData();
  }

  async ngAfterViewInit() {
    this.fourCornerEnabled = await this.domibusInfoService.isFourCornerEnabled();
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
        width: 275
      },
      {
        name: 'From Party Id'
      },
      {
        name: 'To Party Id'
      },
      {
        name: 'Message Status',
        width: 175
      },
      {
        name: 'Notification Status',
        width: 175
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Received',
        width: 155
      },
      {
        name: 'AP Role',
        prop: 'mshRole'
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts'
      },
      {
        cellTemplate: this.nextAttemptInfoTpl,
        name: 'Send Attempts Max'
      },
      {
        cellTemplate: this.nextAttemptInfoWithDateFormatTpl,
        name: 'Next Attempt',
        width: 155
      },
      {
        name: 'Conversation Id',
        cellTemplate: this.rawTextTpl,
      },
      {
        name: 'Message Type',
        width: 130
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Deleted',
        width: 155
      },
      {
        name: 'Action',
        prop: 'action'
      },
      {
        name: 'Service Type',
        prop: 'serviceType'
      },
      {
        name: 'Service Value',
        prop: 'serviceValue'
      }
    ];

    if (this.fourCornerEnabled) {
      this.columnPicker.allColumns.push(
        {
          name: 'Original Sender',
          cellTemplate: this.rawTextTpl
        },
        {
          name: 'Final Recipient',
          cellTemplate: this.rawTextTpl
        });
    }

    this.columnPicker.allColumns.push(
      {
        name: 'Ref To Message Id',
        cellTemplate: this.rawTextTpl,
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Failed',
        width: 155
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Restored',
        width: 155
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 80,
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

  public setServerResults(result: MessageLogResult) {
    this.calculateCount(result);
    super.rows = result.messageLogEntries;

    if (result.filter.receivedFrom) {
      result.filter.receivedFrom = new Date(result.filter.receivedFrom);
    }
    if (result.filter.receivedTo) {
      result.filter.receivedTo = new Date(result.filter.receivedTo);
    }
    if (result.filter.receivedFrom && result.filter.receivedTo) {
      const diff = (result.filter.receivedTo.valueOf() - result.filter.receivedFrom.valueOf()) / this.MS_PER_MINUTE;
      this._messageInterval = this.messageIntervals.find(el => el.value == diff);
    }

    super.filter = result.filter;

    this.mshRoles = result.mshRoles;
    this.msgTypes = result.msgTypes;
    this.msgStatuses = result.msgStatus.sort();
    this.notifStatus = result.notifStatus;
  }

  protected onBeforeFilter() {
    this.setDatesFromInterval();
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

  resend(messageId: string) {
    console.log('Resending message with id ', messageId);

    let url = MessageLogComponent.RESEND_URL.replace('${messageId}', encodeURIComponent(messageId));

    this.http.put(url, {}, {}).subscribe(res => {
      this.alertService.success('The operation resend message completed successfully');
      setTimeout(() => {
        this.messageResent.emit();
      }, 500);
    }, err => {
      this.alertService.exception('The message ' + this.alertService.escapeHtml(messageId) + ' could not be resent.', err);
    });
  }

  isResendButtonEnabledAction(row): boolean {
    return this.isRowResendButtonEnabled(row);
  }

  isResendButtonEnabled() {
    return this.isOneRowSelected() && !this.selected[0].deleted
      && this.isRowResendButtonEnabled(this.selected[0]);
  }

  private isRowResendButtonEnabled(row): boolean {
    return !row.deleted
      && (row.messageStatus === 'SEND_FAILURE' || this.isResendButtonEnabledForSendEnqueued(row))
      && !this.isSplitAndJoinMessage(row);
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

  isDownloadButtonEnabledAction(row): boolean {
    return this.isRowDownloadButtonEnabled(row);
  }

  isDownloadButtonEnabled(): boolean {
    return this.isOneRowSelected() && this.isRowDownloadButtonEnabled(this.selected[0]);
  }

  private isRowDownloadButtonEnabled(row): boolean {
    return !row.deleted && row.messageType !== 'SIGNAL_MESSAGE'
      && !this.isSplitAndJoinMessage(row);
  }

  private isOneRowSelected() {
    return this.selected && this.selected.length == 1;
  }

  private async downloadMessage(row) {
    const messageId = row.messageId;
    let canDownloadUrl = MessageLogComponent.CAN_DOWNLOAD_MESSAGE_URL.replace('${messageId}', encodeURIComponent(messageId));
    this.http.get(canDownloadUrl).subscribe(res => {

      const downloadUrl = MessageLogComponent.DOWNLOAD_MESSAGE_URL.replace('${messageId}', encodeURIComponent(messageId));
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
      this.downloadEnvelopesForUserMessage(row.messageId);
    } else {
      this.downloadEnvelopesForSignalMessage(row);
    }
  }

  private downloadEnvelopesForSignalMessage(row) {
    this.downloadEnvelopesForUserMessage(row.refToMessageId);
  }

  private downloadEnvelopesForUserMessage(messageId) {
    try {
      const downloadUrl = MessageLogComponent.DOWNLOAD_ENVELOPE_URL
        .replace('${messageId}', encodeURIComponent(messageId));

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
    this.dialog.open(MessagelogDetailsComponent, {
      data: {message: selectedRow, fourCornerEnabled: this.fourCornerEnabled}
    });
  }

  onResetAdvancedSearchParams() {
    this.filter.messageType = this.msgTypes[1];
    this.conversationIdValue = null;
    this.notificationStatusValue = null;
    this.setCustomMessageInterval();
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
    this.setCustomMessageInterval();
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
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
}

interface DateInterval {
  value: number
  text: string
}
