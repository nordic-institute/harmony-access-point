import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, EventEmitter, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {MessagesRequestRO} from './ro/messages-request-ro';
import {MatDialog, MatDialogRef} from '@angular/material';
import {MoveDialogComponent} from './move-dialog/move-dialog.component';
import {MessageDialogComponent} from './message-dialog/message-dialog.component';
import {DirtyOperations} from '../common/dirty-operations';
import {Observable} from 'rxjs/Observable';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {DialogsService} from '../common/dialogs/dialogs.service';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ClientSortableListMixin} from '../common/mixins/sortable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
export class JmsComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ClientPageableListMixin, ModifiableListMixin, ClientSortableListMixin)
  implements OnInit, DirtyOperations, AfterViewInit, AfterViewChecked {

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

  markedForDeletionMessages: any[];

  request: MessagesRequestRO;

  private _selectedSource: any;

  get selectedSource(): any {
    return this._selectedSource;
  }

  set selectedSource(value: any) {
    const oldVal = this._selectedSource;
    this._selectedSource = value;
    this.filter.source = value.name;
    this.defaultQueueSet.emit(oldVal);
  }

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    super.filter = new MessagesRequestRO();

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

    this.markedForDeletionMessages = [];

    this.loadDestinations();

    this.queuesInfoGot.subscribe(result => {
      this.setDefaultQueue('.*?[d|D]omibus.?DLQ');
    });

    this.defaultQueueSet.subscribe(oldVal => {
      super.tryFilter().then(done => {
        if (!done) {
          //revert the drop-down value to the old one
          this._selectedSource = oldVal;
        }
      });
    });
  }

  public get name(): string {
    return 'JMS Messages';
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
    if (!this.queues || this.queues.length == 0) {
      return;
    }

    const matching = this.queues.find((el => el.name && el.name.match(queueName)));
    const toSelect = matching != null ? matching : this.queues.length[0];

    this.selectedSource = toSelect;
  }

  edit(row) {
    this.showDetails(row);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  canSearch() {
    return this.filter.source && !this.isLoading;
  }

  protected get GETUrl(): string {
    return 'rest/jms/messages';
  }

  protected async onBeforeGetData(): Promise<any> {
    if (!this.filter.source) {
      return Promise.reject('Source should be set');
    }

    this.markedForDeletionMessages = [];
    this.currentSearchSelectedSource = this.selectedSource;
  }

  protected onLoadDataError(error) {
    this.alertService.error('An error occurred while loading the JMS messages. In case you are using the Selector / JMS Type, please follow the rules for Selector / JMS Type according to Help Page / Admin Guide');
    console.log('Error: ', error.status, error.error);
    error.handled = true;
  }

  public setServerResults(res) {
    super.rows = res.messages;
    super.count = res.messages.length;
    this.refreshDestinations();
  }

  async doSave(): Promise<any> {
    let messageIds = this.markedForDeletionMessages.map((message) => message.id);
    //because the user can change the source after pressing search and then select the messages and press delete
    //in this case I need to use currentSearchSelectedSource
    return this.serverRemove(this.currentSearchSelectedSource.name, messageIds);
  }

  move() {
    const dialogRef: MatDialogRef<MoveDialogComponent> = this.dialog.open(MoveDialogComponent);

    if (/DLQ/.test(this.currentSearchSelectedSource.name)) {

      if (this.selected.length > 1) {
        dialogRef.componentInstance.queues.push(...this.queues);
      } else {
        for (let message of this.selected) {

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
        let messageIds = this.selected.map((message) => message.id);
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
        let messageIds = this.selected.map((message) => message.id);
        this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
      }
    });
  }

  showDetails(selectedRow: any) {
    let dialogRef: MatDialogRef<MessageDialogComponent> = this.dialog.open(MessageDialogComponent);
    dialogRef.componentInstance.message = selectedRow;
    dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
  }

  deleteAction(row) {
    this.markedForDeletionMessages.push(row);
    let newRows = this.rows.filter((element) => {
      return row !== element;
    });
    super.selected = [];
    super.rows = newRows;
    super.count = newRows.length;
  }

  delete() {
    this.markedForDeletionMessages.push(...this.selected);
    let newRows = this.rows.filter((element) => {
      return !this.selected.includes(element);
    });
    super.selected = [];
    super.rows = newRows;
    super.count = newRows.length;
  }

  serverMove(source: string, destination: string, messageIds: Array<any>) {
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
          return !this.selected.includes(element);
        });
        super.selected = [];
        super.rows = newRows;
        super.count = newRows.length;
      },
      error => {
        this.alertService.exception('The operation \'move messages\' could not be completed: ', error);
      }
    )
  }

  serverRemove(source: string, messageIds: Array<any>): Promise<any> {
    return this.http.post('rest/jms/messages/action', {
      source: source,
      selectedMessages: messageIds,
      action: 'REMOVE'
    }).toPromise().then(() => {
        this.refreshDestinations();
        this.markedForDeletionMessages = [];
      }
    )
  }

  saveAsCSV() {
    if (!this.activeFilter.source) {
      this.alertService.error('Source should be set');
      return;
    }

    super.saveAsCSV();
  }

  get csvUrl(): string {
    return 'rest/jms/csv?' + this.createAndSetParameters();
  }

  isDirty(): boolean {
    return this.markedForDeletionMessages && this.markedForDeletionMessages.length > 0;
  }

  canCancel() {
    return (this.markedForDeletionMessages.length > 0);
  }

  canSave() {
    return this.canCancel();
  }

  canDelete() {
    return (this.selected.length > 0);
  }
}
