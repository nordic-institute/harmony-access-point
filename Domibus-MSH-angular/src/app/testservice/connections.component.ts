import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {MessageLogEntry} from '../messagelog/support/messagelogentry';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import * as moment from 'moment';
import {ConnectionMonitorEntry, ConnectionsMonitorService} from './support/connectionsmonitor.service';
import {MatDialog} from '@angular/material';
import {ConnectionDetailsComponent} from './connection-details/connection-details.component';

@Component({
  moduleId: module.id,
  templateUrl: 'connections.component.html',
  styleUrls: ['connections.component.css'],
  providers: [ConnectionsMonitorService]
})

export class ConnectionsComponent extends mix(BaseListComponent).with(ClientPageableListMixin)
  implements OnInit {

  // static readonly TEST_SERVICE_URL: string = 'rest/testservice';
  // static readonly TEST_SERVICE_PARTIES_URL: string = ConnectionsComponent.TEST_SERVICE_URL + '/parties';
  // static readonly TEST_SERVICE_SENDER_URL: string = ConnectionsComponent.TEST_SERVICE_URL + '/sender';
  // static readonly TEST_SERVICE_SUBMIT_DYNAMICDISCOVERY_URL: string = ConnectionsComponent.TEST_SERVICE_URL + '/dynamicdiscovery';

  // static readonly MESSAGE_LOG_LAST_TEST_SENT_URL: string = 'rest/messagelog/test/outgoing/latest';
  // static readonly MESSAGE_LOG_LAST_TEST_RECEIVED_URL: string = 'rest/messagelog/test/incoming/latest';

  // filter: any;

  // messageInfoSent: MessageLogEntry;
  // messageInfoReceived: MessageLogEntry;

  // sender: string;

  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('monitorStatus', {static: false}) monitorStatusTemplate: TemplateRef<any>;
  @ViewChild('connectionStatus', {static: false}) connectionStatusTemplate: TemplateRef<any>;

  constructor(private connectionsMonitorService: ConnectionsMonitorService, private alertService: AlertService,
              private dialog: MatDialog, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    // this.filter = {};
    // this.sender = '';

    this.loadServerData();
  }

  public get name(): string {
    return 'Connection Monitoring';
  }

  ngAfterViewInit() {
    this.initColumns();
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private async getDataAndSetResults() {
    let rows = await this.connectionsMonitorService.getMonitors();
    super.rows = rows;
    super.count = this.rows.length;
  }

  private initColumns() {

    this.columnPicker.allColumns = [
      {
        name: 'Party',
        prop: 'partyId',
        width: 10
      },
      {
        cellTemplate: this.monitorStatusTemplate,
        name: 'Monitoring',
        prop: 'monitorStatus',
        width: 20,
        canAutoResize: true,
        sortable: false
      },
      {
        cellTemplate: this.connectionStatusTemplate,
        name: 'Connection Status',
        prop: 'connectionStatus',
        width: 170,
        canAutoResize: true,
        sortable: false
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        prop: 'actions',
        width: 60,
        canAutoResize: true,
        sortable: false
      }
    ];
    this.columnPicker.selectedColumns = this.columnPicker.allColumns;
  }

  formatDate(dt) {
    // TODO : add a pipe
    return moment(dt).fromNow();
  }

  async toggleConnectionMonitor(row: ConnectionMonitorEntry) {
    let newMonitoredValue = !row.monitored;
    await this.connectionsMonitorService.setMonitorState(row.partyId, newMonitoredValue);
    row.monitored = newMonitoredValue;
    this.alertService.success(`Monitoring ${(newMonitoredValue ? 'enabled' : 'disabled')} for <b> ${row.partyId}</b>`);
  }

  async sendTestMessage(row: ConnectionMonitorEntry) {
    row.status = 'PENDING';
    let messageId = await this.connectionsMonitorService.sendTestMessage(row.partyId);
    await this.refreshMonitor(row);

    if (row.status == 'PENDING') {
      setTimeout(() => this.refreshMonitor(row), 1500);
    }
  }

  async refreshMonitor(row: ConnectionMonitorEntry) {
    let refreshedRow = await this.connectionsMonitorService.getMonitor(row.partyId);
    Object.assign(row, refreshedRow);
  }

  openDetails(row: ConnectionMonitorEntry) {
    this.dialog.open(ConnectionDetailsComponent, {data: {partyId: row.partyId}}).afterClosed().subscribe(result => {
      this.refreshMonitor(row);
    });
  }
}
