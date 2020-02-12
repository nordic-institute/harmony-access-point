import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PartyResponseRo, PartyFilteredResult, ProcessRo, CertificateRo} from './party';
import {Observable} from 'rxjs/Observable';

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Injectable()
export class PartyService {
  static readonly CERTIFICATE: string = 'rest/party/{partyName}/certificate';
  static readonly LIST_PROCESSES: string = 'rest/party/processes';
  static readonly LIST_PARTIES: string = 'rest/party/list?pageSize=0';
  static readonly UPDATE_PARTIES: string = 'rest/party/update';
  static readonly CSV_PARTIES: string = 'rest/party/csv';

  constructor (private http: HttpClient) {
  }

  uploadCertificate (payload, partyName: string): Observable<CertificateRo> {
    return this.http.put<CertificateRo>(PartyService.CERTIFICATE.replace('{partyName}', partyName), payload);
  }

  getCertificate (partyName: string): Observable<CertificateRo> {
    return this.http.get<CertificateRo>(PartyService.CERTIFICATE.replace('{partyName}', partyName));
  }

  async getData(activeFilter): Promise<any> {
    var serverCalls: [Promise<PartyFilteredResult>, Promise<ProcessRo[]>] = [
      this.listParties(activeFilter.name, activeFilter.endPoint,
        activeFilter.partyID, activeFilter.process, activeFilter.process_role).toPromise(),
      this.listProcesses().toPromise()
    ];
    return Promise.all(serverCalls);
  }

  listProcesses (): Observable<ProcessRo[]> {
    return this.http.get<ProcessRo[]>(PartyService.LIST_PROCESSES)
      .catch(() => Observable.throw('No processes found'));
  }

  listParties (name: string, endPoint: string, partyId: string, process: string, process_role: string)
    : Observable<PartyFilteredResult> {

    return this.http.get<PartyResponseRo[]>(PartyService.LIST_PARTIES).map(allRecords => {
      let records = allRecords.slice();

      if (name) {
        records = records.filter(party => party.name === name);
      }
      if (endPoint) {
        records = records.filter(party => party.endpoint === endPoint);
      }
      if (partyId) {
        records = records.filter(party => party.identifiers.filter(x => x.partyId === partyId).length > 0);
      }
      if (process) {
        let query: string = process + '(';
        records = records.filter(party => party.joinedProcesses.indexOf(query) >= 0);
      }
      if (process_role) {
        let query1: string = (process || '') + process_role;
        let query2: string = (process || '') + '(IR)';
        records = records.filter(party => party.joinedProcesses.indexOf(query1) >= 0 || party.joinedProcesses.indexOf(query2) >= 0);
      }

      return {data: records, allData: allRecords};
    }).catch(() => Observable.throw('No parties found'));

  }

  getFilterPath (name: string, endPoint: string, partyId: string, process: string) {
    let result = '?';

    if (name) {
      result += 'name=' + name + '&';
    }
    if (endPoint) {
      result += 'endPoint=' + endPoint + '&';
    }
    if (partyId) {
      result += 'partyId=' + partyId + '&';
    }
    if (process) {
      result += 'process=' + process + '&';
    }
    return result;
  }

  initParty () {
    const newParty = new PartyResponseRo();
    newParty.processesWithPartyAsInitiator = [];
    newParty.processesWithPartyAsResponder = [];
    newParty.identifiers = [];
    return newParty;
  }

  validateParties (partyList: PartyResponseRo[]) {
    const partiesWithoutIdentifiers = partyList.filter(party => party.identifiers == null || party.identifiers.length === 0);
    if (partiesWithoutIdentifiers.length > 0) {
      const names = partiesWithoutIdentifiers.map(party => party.name).join(',');
      throw new Error('The following parties do not have any identifiers set:' + names);
    }
  }

  updateParties (partyList: PartyResponseRo[]): Promise<any> {
    return this.http.put(PartyService.UPDATE_PARTIES, partyList).toPromise();
  }
}
