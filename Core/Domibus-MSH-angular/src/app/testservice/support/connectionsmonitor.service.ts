import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';
import {PartyResponseRo} from '../../party/support/party';
import {PropertiesService, PropertyModel} from '../../properties/support/properties.service';

/**
 * @Author Dussart Thomas
 * @Since 3.3
 */

@Injectable()
export class ConnectionsMonitorService {

  static readonly ALL_PARTIES_URL: string = 'rest/party/list?pageSize=0';

  static readonly TEST_SERVICE_URL: string = 'rest/testservice';
  static readonly TEST_SERVICE_PARTIES_URL: string = ConnectionsMonitorService.TEST_SERVICE_URL + '/parties';
  static readonly TEST_SERVICE_SENDER_URL: string = ConnectionsMonitorService.TEST_SERVICE_URL + '/sender';
  static readonly CONNECTION_MONITOR_URL: string = ConnectionsMonitorService.TEST_SERVICE_URL + '/connectionmonitor';
  static readonly TEST_SERVICE_ERRORS_URL: string = ConnectionsMonitorService.TEST_SERVICE_URL + '/errors';


  constructor(private http: HttpClient, private alertService: AlertService, private propertiesService: PropertiesService) {
  }

  async getMonitors(currentSenderPartyId: any): Promise<ConnectionMonitorEntry[]> {
    let allParties = await this.http.get<PartyResponseRo[]>(ConnectionsMonitorService.ALL_PARTIES_URL).toPromise();
    if (!allParties || !allParties.length) {
      this.alertService.error('The Pmode is not properly configured.');
      return [];
    }

    let parties = await this.http.get<any[]>(ConnectionsMonitorService.TEST_SERVICE_PARTIES_URL).toPromise();

    if (!parties || !parties.length) {
      const error = 'Could not find testable parties. Self-party could not be an initiator of the test process.';
      this.alertService.error(error);
    }

    let monitors = await this.getMonitorsForParties(currentSenderPartyId, parties);
    const result: ConnectionMonitorEntry[] = [];
    allParties.forEach(party => {
      party.identifiers
        .sort((id1, id2) => id1.partyId.localeCompare(id2.partyId))
        .forEach(partyId => {
          let cmEntry: ConnectionMonitorEntry = new ConnectionMonitorEntry();
          let partyId1 = partyId.partyId;
          cmEntry.partyId = partyId1;
          cmEntry.partyName = party.name + '(' + partyId1 + ')';
          Object.assign(cmEntry, monitors[partyId1]);
          result.push(cmEntry);
        })
    });
    return result;
  }

  async getMonitor(senderPartyId: string, partyId: string): Promise<ConnectionMonitorEntry> {
    let monitors = await this.getMonitorsForParties(senderPartyId, [partyId]);
    console.log('monitors ', monitors);
    return monitors[partyId];
  }

  private getMonitorsForParties(senderPartyId: string, partyIds: string[]): Promise<Map<string, ConnectionMonitorEntry>> {
    if (!partyIds.length) {
      return new Promise<Map<string, ConnectionMonitorEntry>>((resolve, reject) => resolve(new Map()));
    }
    let url = ConnectionsMonitorService.CONNECTION_MONITOR_URL;
    let searchParams = new HttpParams();
    searchParams = searchParams.append('senderPartyId', senderPartyId)
    partyIds.forEach(partyId => searchParams = searchParams.append('partyIds', partyId));
    return this.http.get<Map<string, ConnectionMonitorEntry>>(url, {params: searchParams}).toPromise();
  }

  getSenderParty(): Promise<PartyResponseRo> {
    return this.http.get<PartyResponseRo>(ConnectionsMonitorService.TEST_SERVICE_SENDER_URL).toPromise();
  }

  async sendTestMessage(receiverPartyId: string, senderPartyId: String) {
    const payload = {sender: senderPartyId, receiver: receiverPartyId};
    return await this.http.post<string>(ConnectionsMonitorService.TEST_SERVICE_URL, payload).toPromise();
  }

  async setMonitorState(senderPartyId: string, partyId: string, enabled: boolean) {
    let propName = 'domibus.monitoring.connection.party.enabled';
    await this.setState(senderPartyId, enabled, partyId, propName);
  }

  async setAlertableState(senderPartyId: string, partyId: string, enabled: boolean) {
    let propName = 'domibus.alert.connection.monitoring.parties';
    await this.setState(senderPartyId, enabled, partyId, propName);
  }

  private async setState(senderPartyId: string, enabled: boolean, partyId: string, propName: string) {
    let testableParties = await this.http.get<string[]>(ConnectionsMonitorService.TEST_SERVICE_PARTIES_URL).toPromise();
    if (!testableParties || !testableParties.length) {
      throw new Error('The test service is not properly configured.');
    }
    if (enabled && !testableParties.includes(partyId)) {
      throw new Error(partyId + ' is not configured for testing');
    }

    let prop: PropertyModel = await this.propertiesService.getProperty(propName);

    let enabledParties: string[] = prop.value.split(',').map(p => p.trim()).filter(p => p.toLowerCase() != partyId.toLowerCase());
    // remove old parties that are no longer testable:
    enabledParties = enabledParties.filter(ep => ep.split('>').every(p => testableParties.includes(p)));

    let value = senderPartyId + '>' + partyId;
    if (enabled) {
      enabledParties.push(value);
    } else {
      enabledParties = enabledParties.filter(el => el != value);
    }
    prop.value = enabledParties.join(',');
    await this.propertiesService.updateProperty(prop);
  }

  async setMonitorStateForAll(senderPartyId: string, list: ConnectionMonitorEntry[], enabled: boolean) {
    let propName = 'domibus.monitoring.connection.party.enabled';
    await this.setStateForAll(propName, enabled, list, senderPartyId);
  }

  async setAlertableStateForAll(senderPartyId: string, list: ConnectionMonitorEntry[], enabled: boolean) {
    let propName = 'domibus.alert.connection.monitoring.parties';
    await this.setStateForAll(propName, enabled, list, senderPartyId);
  }

  private async setStateForAll(propName: string, enabled: boolean, list: ConnectionMonitorEntry[], senderPartyId: string) {
    let prop: PropertyModel = await this.propertiesService.getProperty(propName);
    if (enabled) {
      prop.value = list.map(el => senderPartyId + '>' + el.partyId).join(',');
    } else {
      prop.value = '';
    }
    await this.propertiesService.updateProperty(prop);
  }

  async setDeleteHistoryState(senderPartyId: string, partyId: string, enabled: boolean) {
    let propName = 'domibus.monitoring.connection.party.history.delete';
    await this.setState(senderPartyId, enabled, partyId, propName);
  }

  async setDeleteHistoryStateForAll(senderPartyId: string, list: ConnectionMonitorEntry[], enabled: boolean) {
    let propName = 'domibus.monitoring.connection.party.history.delete';
    await this.setStateForAll(propName, enabled, list, senderPartyId);
  }
}

export class ConnectionMonitorEntry {
  senderPartyName?: string;
  senderPartyId?: string;
  partyId: string;
  partyName?: string;
  testable: boolean;
  monitored: boolean;
  alertable: boolean;
  deleteHistory: boolean;
  status: string;
  lastSent: any;
  lastReceived: any;
  error?: string;
}

