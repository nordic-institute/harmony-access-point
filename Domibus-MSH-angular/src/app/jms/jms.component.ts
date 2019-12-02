import {ChangeDetectorRef, Component, EventEmitter, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {MessagesRequestRO} from './ro/messages-request-ro';
import {MatDialog, MatDialogRef} from '@angular/material';
import {MoveDialogComponent} from './move-dialog/move-dialog.component';
import {MessageDialogComponent} from './message-dialog/message-dialog.component';
import {DirtyOperations} from '../common/dirty-operations';
import {ColumnPickerBase} from '../common/column-picker/column-picker-base';
import {RowLimiterBase} from '../common/row-limiter/row-limiter-base';
import {Observable} from 'rxjs/Observable';
import {DownloadService} from '../common/download.service';
import {AlertComponent} from '../common/alert/alert.component';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {DialogsService} from '../common/dialogs/dialogs.service';

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent extends mix(BaseListComponent).with(FilterableListMixin) implements OnInit, DirtyOperations {

  columnPicker: ColumnPickerBase = new ColumnPickerBase();
  rowLimiter: RowLimiterBase = new RowLimiterBase();

  dateFormat: String = 'yyyy-MM-dd HH:mm:ssZ';

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  defaultQueueSet: EventEmitter<boolean>;
  queuesInfoGot: EventEmitter<boolean>;

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<Object>;
  @ViewChild('rowWithJSONTpl', {static: false}) rowWithJSONTpl: TemplateRef<Object>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;

  queues: any[];
  orderedQueues: any[];

  currentSearchSelectedSource;

  selectedMessages: any[];
  markedForDeletionMessages: any[];
  loading: boolean;

  rows: Array<any>;
  request: MessagesRequestRO;

  private _selectedSource: any;
  offset: any;

  get selectedSource(): any {
    return this._selectedSource;
  }

  set selectedSource(value: any) {
    var oldVal = this._selectedSource;
    this._selectedSource = value;
    this.filter.source = value.name;
    this.defaultQueueSet.emit(oldVal);
  }

  constructor(private http: HttpClient, private alertService: AlertService, public dialog: MatDialog,
              private dialogsService: DialogsService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    this['filter'] = new MessagesRequestRO();

    this.offset = 0;
    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

    this.defaultQueueSet = new EventEmitter(false);
    this.queuesInfoGot = new EventEmitter(false);

    this.queues = [];
    this.orderedQueues = [];

    // set toDate equals to now
    this.filter.toDate = new Date();
    this.filter.toDate.setHours(23, 59, 59, 999);

    this.selectedMessages = [];
    this.markedForDeletionMessages = [];
    this.loading = false;

    this.rows = [];

    this.loadDestinations();

    this.queuesInfoGot.subscribe(result => {
      this.setDefaultQueue('.*?[d|D]omibus.?DLQ');
    });

    this.defaultQueueSet.subscribe(oldVal => {
      super.trySearch().then(done => {
        if (!done) {
          //revert the drop-down value to the old one
          this._selectedSource = oldVal;
        }
      });
    });
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'ID',
        prop: 'id'
      },
      {
        name: 'JMS Type',
        prop: 'type',
        width: 80
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Time',
        prop: 'timestamp',
        width: 80
      },
      {
        name: 'Content',
        prop: 'content'

      },
      {
        cellTemplate: this.rowWithJSONTpl,
        name: 'Custom prop',
        prop: 'customProperties',
        width: 250
      },
      {
        cellTemplate: this.rowWithJSONTpl,
        name: 'JMS prop',
        prop: 'jmsproperties',
        width: 200
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 10,
        sortable: false
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['ID', 'Time', 'Custom prop', 'JMS prop', 'Actions'].indexOf(col.name) != -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private getDestinations(): Observable<any> {
    return this.http.get<any>('rest/jms/destinations')
      .map(response => response.jmsDestinations)
      .catch((error) => this.alertService.exception('Could not load queues ', error));
  }

  private loadDestinations() {
    this.getDestinations().subscribe(
      (destinations) => {
        this.queues = [];
        for (const key in destinations) {
          this.queues.push(destinations[key]);
        }
        this.queuesInfoGot.emit();
      }
    );
  }

  private refreshDestinations(): Observable<any> {
    const result = this.getDestinations();
    result.subscribe(
      (destinations) => {
        for (const key in destinations) {
          var src = destinations[key];
          const queue = this.queues.find(el => el.name === src.name);
          if (queue) {
            Object.assign(queue, src);
          }
        }
      }
    );
    return result;
  }

  private setDefaultQueue(queueName: string) {
    if (!this.queues || this.queues.length == 0) return;

    const matching = this.queues.find((el => el.name && el.name.match(queueName)));
    const toSelect = matching != null ? matching : this.queues.length[0];

    this.selectedSource = toSelect;
  }

  changePageSize(newPageSize: number) {
    super.resetFilters();
    this.offset = 0;
    this.rowLimiter.pageSize = newPageSize;
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

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  canSearch() {
    return this.filter.source && !this.loading;
  }

  search() {
    super.setActiveFilter();
    this.doSearch();
  }

  private doSearch() {
    if (!this.filter.source) {
      this.alertService.error('Source should be set');
      return;
    }
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.selectedMessages = [];
    this.markedForDeletionMessages = [];
    this.currentSearchSelectedSource = this.selectedSource;
    this.http.post<any>('rest/jms/messages', {
      source: this.activeFilter.source,
      jmsType: this.activeFilter.jmsType,
      fromDate: this.activeFilter.fromDate,
      toDate: this.activeFilter.toDate,
      selector: this.activeFilter.selector,
    }).subscribe(
      res => {
        this.rows = res.messages;
        this.offset = 0;
        this.refresh();
        this.loading = false;

        this.refreshDestinations();
      },
      error => {
        this.alertService.exception('An error occurred. In case you are using the Selector / JMS Type, please follow the rules for Selector / JMS Type according to Help Page / Admin Guide. ', error);
        this.loading = false;
      }
    );
  }

  async cancel() {
    const cancel = await this.dialogsService.openCancelDialog();
    if (cancel) {
      super.resetFilters();
      this.doSearch();
    }
  }

  save() {
    let messageIds = this.markedForDeletionMessages.map((message) => message.id);
    //because the user can change the source after pressing search and then select the messages and press delete
    //in this case I need to use currentSearchSelectedSource
    this.serverRemove(this.currentSearchSelectedSource.name, messageIds);
  }

  move() {
    const dialogRef: MatDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {

      if (this.selectedMessages.length > 1) {
        dialogRef.componentInstance.queues.push(...this.queues);
      } else {
        for (let message of this.selectedMessages) {

          try {
            let originalQueue = message.customProperties.originalQueue;
            // EDELIVERY-2814
            let originalQueueName = originalQueue.substr(originalQueue.indexOf('!') + 1);
            if (originalQueue) {
              let queues = this.queues.filter((queue) => queue.name.indexOf(originalQueueName) != -1);
              console.debug(queues);
              if (queues) {
                dialogRef.componentInstance.queues = queues;
                dialogRef.componentInstance.selectedSource = queues[0];
              }
              if (queues.length == 1) {
                dialogRef.componentInstance.destinationsChoiceDisabled = true;
              }
              break;
            }
          } catch (e) {
            console.error(e);
          }
        }


        if (dialogRef.componentInstance.queues.length == 0) {
          console.warn('Unable to determine the original queue for the selected messages');
          dialogRef.componentInstance.queues.push(...this.queues);
        }
      }
    } else {
      dialogRef.componentInstance.queues.push(...this.queues);
    }


    dialogRef.afterClosed().subscribe(result => {
      if (result && result.destination) {
        let messageIds = this.selectedMessages.map((message) => message.id);
        this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
      }
    });
  }

  moveAction(row) {
    let dialogRef: MatDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {
      try {
        let originalQueue = row.customProperties.originalQueue;
        // EDELIVERY-2814
        let originalQueueName = originalQueue.substr(originalQueue.indexOf('!') + 1);
        let queues = this.queues.filter((queue) => queue.name.indexOf(originalQueueName) != -1);
        console.debug(queues);
        if (queues) {
          dialogRef.componentInstance.queues = queues;
          dialogRef.componentInstance.selectedSource = queues[0];
        }
        if (queues.length == 1) {
          dialogRef.componentInstance.destinationsChoiceDisabled = true;
        }
      } catch (e) {
        console.error(e);
      }

      if (dialogRef.componentInstance.queues.length == 0) {
        console.log(dialogRef.componentInstance.queues.length);
        dialogRef.componentInstance.queues.push(...this.queues);
      }
    } else {
      dialogRef.componentInstance.queues.push(...this.queues);
    }

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.destination) {
        let messageIds = this.selectedMessages.map((message) => message.id);
        this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
      }
    });
  }

  details(selectedRow: any) {
    let dialogRef: MatDialogRef<MessageDialogComponent> = this.dialog.open(MessageDialogComponent);
    dialogRef.componentInstance.message = selectedRow;
    dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
    dialogRef.afterClosed().subscribe(result => {
      //Todo:
    });
  }

  deleteAction(row) {
    this.markedForDeletionMessages.push(row);
    let newRows = this.rows.filter((element) => {
      return row !== element;
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  delete() {
    this.markedForDeletionMessages.push(...this.selectedMessages);
    let newRows = this.rows.filter((element) => {
      return !this.selectedMessages.includes(element);
    });
    this.selectedMessages = [];
    this.rows = newRows;
  }

  serverMove(source: string, destination: string, messageIds: Array<any>) {
    console.log('serverMove');
    this.http.post('rest/jms/messages/action', {
      source: source,
      destination: destination,
      selectedMessages: messageIds,
      action: 'MOVE'
    }).subscribe(
      () => {
        this.alertService.success('The operation \'move messages\' completed successfully.');

        //refresh destinations
        this.refreshDestinations().subscribe(res => {
          this.setDefaultQueue(this.currentSearchSelectedSource.name);
        });

        //remove the selected rows
        let newRows = this.rows.filter((element) => {
          return !this.selectedMessages.includes(element);
        });
        this.selectedMessages = [];
        this.rows = newRows;
      },
      error => {
        this.alertService.exception('The operation \'move messages\' could not be completed: ', error);
      }
    )
  }

  serverRemove(source: string, messageIds: Array<any>) {
    this.http.post('rest/jms/messages/action', {
      source: source,
      selectedMessages: messageIds,
      action: 'REMOVE'
    }).subscribe(
      () => {
        this.alertService.success('The operation \'updates on message(s)\' completed successfully.');
        this.refreshDestinations();
        this.markedForDeletionMessages = [];
      },
      error => {
        this.alertService.exception('The operation \'updates on message(s)\' could not be completed: ', error);
      }
    )
  }

  getFilterPath() {
    let result = '?';
    if (this.activeFilter.source) {
      result += 'source=' + this.activeFilter.source + '&';
    }
    if (this.activeFilter.jmsType) {
      result += 'jmsType=' + this.activeFilter.jmsType + '&';
    }
    if (this.activeFilter.fromDate) {
      result += 'fromDate=' + this.activeFilter.fromDate.toISOString() + '&';
    }
    if (this.activeFilter.toDate) {
      result += 'toDate=' + this.activeFilter.toDate.toISOString() + '&';
    }
    if (this.activeFilter.selector) {
      result += 'selector=' + this.activeFilter.selector + '&';
    }
    return result;
  }

  saveAsCSV() {
    // TODO: await saveIfNeeded ??

    if (!this.activeFilter.source) {
      this.alertService.error('Source should be set');
      return;
    }
    if (this.rows.length > AlertComponent.MAX_COUNT_CSV) {
      this.alertService.error(AlertComponent.CSV_ERROR_MESSAGE);
      return;
    }
    super.resetFilters();
    DownloadService.downloadNative('rest/jms/csv' + this.getFilterPath());
  }

  isDirty(): boolean {
    return this.markedForDeletionMessages && this.markedForDeletionMessages.length > 0;
  }

  onPage($event) {
    this.offset = $event.offset;
    super.resetFilters();
  }

  onSort() {
    super.resetFilters();
  }

  toJson(value) {
    return JSON.stringify(value);
  }
}
