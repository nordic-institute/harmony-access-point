import {HttpClient} from '@angular/common/http';
import {AlertService} from 'app/common/alert/alert.service';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {TrustStoreEntry} from './trustore.model';
import * as FileSaver from 'file-saver';

/**
 * @Author Dussart Thomas
 * @Since 3.3
 */

@Injectable()
export class TrustStoreService {

  url = 'rest/truststore';

  constructor(private http: HttpClient, private alertService: AlertService) {

  }

  getEntries(): Promise<TrustStoreEntry[]> {
    return this.http.get<TrustStoreEntry[]>(this.url + '/list').toPromise();
  }

  uploadTrustStore(file, password): Observable<string> {
    let input = new FormData();
    input.append('truststore', file);
    input.append('password', password);
    return this.http.post<string>(this.url + '/save', input);
  }


  /**
   * Local persister for the jks file
   * @param data
   */
  saveTrustStoreFile(data: any) {
    const blob = new Blob([data], {type: 'application/octet-stream'});
    let filename = 'TrustStore.jks';
    FileSaver.saveAs(blob, filename, false);
  }

}
