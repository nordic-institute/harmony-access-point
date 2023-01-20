import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {AlertService} from '../common/alert/alert.service';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import * as moment from 'moment';
import {ConnectionMonitorEntry, ConnectionsMonitorService} from './support/connectionsmonitor.service';
import {MatDialog} from '@angular/material';
import {ConnectionDetailsComponent} from './connection-details/connection-details.component';
import {ApplicationContextService} from '../common/application-context.service';
import {ComponentName} from '../common/component-name-decorator';
import {HttpClient, HttpErrorResponse, HttpParams} from '@angular/common/http';
import {PartyResponseRo} from '../party/support/party';
import {MatSelectChange} from '@angular/material/select';

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
@ComponentName('Connection Monitoring')
export class ConnectionsComponent extends mix(BaseListComponent).with(ClientPageableListMixin)
  implements OnInit, AfterViewInit, AfterViewChecked {

  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('monitorStatus', {static: false}) monitorStatusTemplate: TemplateRef<any>;
  @ViewChild('monitorStatusHeader', {static: false}) monitorStatusHeaderTemplate: TemplateRef<any>;
  @ViewChild('alertableStatus', {static: false}) alertableStatusTemplate: TemplateRef<any>;
  @ViewChild('alertableStatusHeader', {static: false}) alertableStatusHeaderTemplate: TemplateRef<any>;
  @ViewChild('deleteOldStatus', {static: false}) deleteOldStatusTemplate: TemplateRef<any>;
  @ViewChild('deleteOldStatusHeader', {static: false}) deleteOldStatusHeaderTemplate: TemplateRef<any>;
  @ViewChild('connectionStatus', {static: false}) connectionStatusTemplate: TemplateRef<any>;
  allMonitored: boolean;
  allAllertable: boolean;
  allDeleteHistory: boolean;
  currentSenderPartyId: any;
  sender: PartyResponseRo;

  constructor(private applicationService: ApplicationContextService, private connectionsMonitorService: ConnectionsMonitorService,
              private alertService: AlertService, private dialog: MatDialog, private changeDetector: ChangeDetectorRef, private http: HttpClient) {
    super();
  }

  async ngOnInit() {
    super.ngOnInit();

    this.sender = await this.connectionsMonitorService.getSenderParty();
    let partyIds = this.sender.identifiers;
    partyIds.sort((id1, id2) => id1.partyId.localeCompare(id2.partyId));
    this.setCurrentSenderPartyId(partyIds[0].partyId);

    this.loadServerData();
  }

  ngAfterViewInit() {
    this.initColumns();
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private async getDataAndSetResults() {
    let rows: ConnectionMonitorEntry[] = await this.connectionsMonitorService.getMonitors(this.currentSenderPartyId);
    rows.forEach(entry => {
      entry.senderPartyName = this.sender.name + '(' + this.currentSenderPartyId + ')';
      entry.senderPartyId = this.currentSenderPartyId;
    });
    super.rows = rows;
    super.count = this.rows.length;

    this.refreshAllMonitored();
    this.refreshAllAlertable();
    this.refreshAllDeleteOld();
  }

  private refreshAllMonitored() {
    this.allMonitored = this.rows.filter(el => el.testable).every(el => el.monitored);
  }

  private refreshAllAlertable() {
    this.allAllertable = this.rows.filter(el => el.testable).every(el => el.alertable);
  }

  private refreshAllDeleteOld() {
    this.allDeleteHistory = this.rows.filter(el => el.testable).every(el => el.deleteHistory);
  }

  private initColumns() {
    this.columnPicker.allColumns = [
      {
        name: 'Sender Party',
        prop: 'senderPartyName',
        width: 10
      },
      {
        name: 'Responder Party',
        prop: 'partyName',
        width: 10
      },
      {
        cellTemplate: this.monitorStatusTemplate,
        headerTemplate: this.monitorStatusHeaderTemplate,
        name: 'Monitoring',
        prop: 'monitorStatus',
        width: 20,
        canAutoResize: true,
        sortable: false
      },
      {
        cellTemplate: this.alertableStatusTemplate,
        headerTemplate: this.alertableStatusHeaderTemplate,
        name: 'Alert on Fail',
        prop: 'alertableStatus',
        width: 20,
        canAutoResize: true,
        sortable: false
      },
      {
        cellTemplate: this.deleteOldStatusTemplate,
        headerTemplate: this.deleteOldStatusHeaderTemplate,
        name: 'Delete Old',
        prop: 'deleteOldStatus',
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
        width: 30,
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
    let newValue = row.monitored;
    let newValueText = `${(newValue ? 'enabled' : 'disabled')}`;

    try {
      await this.connectionsMonitorService.setMonitorState(row.senderPartyId, row.partyId, newValue);
      row.monitored = newValue;
      this.refreshAllMonitored();
      this.alertService.success(`Monitoring ${newValueText} for <b>${row.partyId}</b>`);
    } catch (err) {
      row.monitored = !newValue;
      this.refreshAllMonitored();
      this.alertService.exception(`Monitoring could not be ${newValueText} for <b>${row.partyId}</b>:<br>`, err);
    }
  }

  async toggleAlertable(row: ConnectionMonitorEntry) {
    let newValue = row.alertable;
    let newValueText = `${(newValue ? 'enabled' : 'disabled')}`;

    try {
      await this.connectionsMonitorService.setAlertableState(row.senderPartyId, row.partyId, newValue);
      row.alertable = newValue;
      this.refreshAllAlertable();
      this.alertService.success(`Alert generation ${newValueText} for <b>${row.partyId}</b>`);
    } catch (err) {
      row.alertable = !newValue;
      this.refreshAllAlertable();
      this.alertService.exception(`Alert generation could not be ${newValueText} for <b>${row.partyId}</b>:<br>`, err);
    }
  }

  async toggleDeleteHistory(row: ConnectionMonitorEntry) {
    let newValue = row.deleteHistory;
    let newValueText = `${(newValue ? 'enabled' : 'disabled')}`;

    try {
      await this.connectionsMonitorService.setDeleteHistoryState(row.senderPartyId, row.partyId, newValue);
      row.deleteHistory = newValue;
      this.refreshAllDeleteOld();
      this.alertService.success(`Delete history ${newValueText} for <b>${row.partyId}</b>`);
    } catch (err) {
      row.deleteHistory = !newValue;
      this.refreshAllDeleteOld();
      this.alertService.exception(`Delete history could not be ${newValueText} for <b>${row.partyId}</b>:<br>`, err);
    }
  }

  async sendTestMessage(row: ConnectionMonitorEntry) {
    row.status = 'PENDING';
    let messageId = await this.connectionsMonitorService.sendTestMessage(row.partyId, row.senderPartyId);
    await this.refreshMonitor(row);
  }

  async refreshMonitor(row: ConnectionMonitorEntry) {
    let refreshedRow = await this.connectionsMonitorService.getMonitor(row.senderPartyId, row.partyId);
    Object.assign(row, refreshedRow);

    if (row.status == 'PENDING') {
      setTimeout(() => this.refreshMonitor(row), 1500);
    }
  }

  openDetails(row: ConnectionMonitorEntry) {
    this.dialog.open(ConnectionDetailsComponent, {
      data: {
        senderPartyId: row.senderPartyId,
        partyId: row.partyId
      }
    }).afterClosed().subscribe(result => {
      this.refreshMonitor(row);
    });
  }


  onCurrentSenderPartyId($event: MatSelectChange) {
    console.log($event)
    this.setCurrentSenderPartyId($event.value);
  }

  private async setCurrentSenderPartyId(value: any) {
    this.currentSenderPartyId = value;
    await this.getDataAndSetResults();
  }

  async toggleMonitorAll() {
    let newState = this.allMonitored;
    let newStateText = `${(newState ? 'enabled' : 'disabled')}`;

    let active: ConnectionMonitorEntry[] = this.rows.filter(row => row.testable);
    active.forEach(row => {
      row['originalMonitored'] = row.monitored;
      row.monitored = newState;
    });
    try {
      await this.connectionsMonitorService.setMonitorStateForAll(this.currentSenderPartyId, active, newState);
      this.alertService.success(`Monitoring ${newStateText} for all parties`);
    } catch (err) {
      active.forEach(row => row.monitored = row['originalMonitored']);
      this.alertService.exception(`Monitoring could not be ${newStateText} for all parties`, err);
    }
  }

  async toggleAlertableAll() {
    let newState = this.allAllertable;
    let newStateText = `${(newState ? 'enabled' : 'disabled')}`;

    let active: ConnectionMonitorEntry[] = this.rows.filter(row => row.testable);
    active.forEach(row => {
      row['originalAlertable'] = row.alertable;
      row.alertable = newState;
    });
    try {
      await this.connectionsMonitorService.setAlertableStateForAll(this.currentSenderPartyId, active, newState);
      this.alertService.success(`Alert generation ${newStateText} for all parties`);
    } catch (err) {
      active.forEach(row => row.alertable = row['originalAlertable']);
      this.alertService.exception(`Alert generation could not be ${newStateText} for all parties`, err);
    }
  }

  async toggleDeleteAllHistory() {
    let newState = this.allDeleteHistory;
    let newStateText = `${(newState ? 'enabled' : 'disabled')}`;

    let active: ConnectionMonitorEntry[] = this.rows.filter(row => row.testable);
    active.forEach(row => {
      row['originalDeleteOld'] = row.deleteHistory;
      row.deleteHistory = newState;
    });
    try {
      await this.connectionsMonitorService.setDeleteHistoryStateForAll(this.currentSenderPartyId, active, newState);
      this.alertService.success(`Delete history ${newStateText} for all parties`);
    } catch (err) {
      active.forEach(row => row.deleteHistory = row['originalDeleteOld']);
      this.alertService.exception(`Delete history could not be ${newStateText} for all parties`, err);
    }
  }


  async showErrors(row) {
    try {
      let searchParams = new HttpParams();
      searchParams = searchParams.append('userMessageId', row.lastSent.messageId);
      let result = await this.http.get<any>('rest/testservice/errors', {params: searchParams}).toPromise();
      if (result) {
        let httpErrorResponse = new HttpErrorResponse({error: result});
        this.alertService.exception('', httpErrorResponse);
      }
    } catch (ex) {
      this.alertService.exception('', ex);
    }
  }

  anyTestable() {
    return this.rows.some(el => el.testable);
  }
}
