import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import * as moment from 'moment';
import {ConnectionMonitorEntry, ConnectionsMonitorService} from './support/connectionsmonitor.service';
import {MatDialog} from '@angular/material';
import {ConnectionDetailsComponent} from './connection-details/connection-details.component';
import {ApplicationContextService} from '../common/application-context.service';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Connections monitor form.
 */
@Component({
  moduleId: module.id,
  templateUrl: 'connections.component.html',
  styleUrls: ['connections.component.css'],
  providers: [ConnectionsMonitorService]
})

export class ConnectionsComponent extends mix(BaseListComponent).with(ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('monitorStatus', {static: false}) monitorStatusTemplate: TemplateRef<any>;
  @ViewChild('connectionStatus', {static: false}) connectionStatusTemplate: TemplateRef<any>;

  constructor(private applicationService: ApplicationContextService, private connectionsMonitorService: ConnectionsMonitorService,
              private alertService: AlertService, private dialog: MatDialog, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

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
        prop: 'partyName',
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
    return dt ? moment(dt).fromNow() : '';
  }

  async toggleConnectionMonitor(row: ConnectionMonitorEntry) {

    let newMonitoredValue = row.monitored;
    let newMonitorState = `${(newMonitoredValue ? 'enabled' : 'disabled')}`;

    try {
      await this.connectionsMonitorService.setMonitorState(row.partyId, newMonitoredValue);
      row.monitored = newMonitoredValue;
      this.alertService.success(`Monitoring ${newMonitorState} for <b>${row.partyId}</b>`);
    } catch (err) {
      row.monitored = !newMonitoredValue;
      this.alertService.exception(`Monitoring could not be ${newMonitorState} for <b>${row.partyId}</b>:<br>`, err);
    }
  }

  async sendTestMessage(row: ConnectionMonitorEntry) {
    row.status = 'PENDING';
    let messageId = await this.connectionsMonitorService.sendTestMessage(row.partyId);
    await this.refreshMonitor(row);
  }

  async refreshMonitor(row: ConnectionMonitorEntry) {
    let refreshedRow = await this.connectionsMonitorService.getMonitor(row.partyId);
    Object.assign(row, refreshedRow);

    if (row.status == 'PENDING') {
      setTimeout(() => this.refreshMonitor(row), 1500);
    }
  }

  openDetails(row: ConnectionMonitorEntry) {
    this.dialog.open(ConnectionDetailsComponent, {data: {partyId: row.partyId}}).afterClosed().subscribe(result => {
      this.refreshMonitor(row);
    });
  }
}
