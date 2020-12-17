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

  constructor(private http: HttpClient, private alertService: AlertService) {
  }

  getEntries(url): Promise<TrustStoreEntry[]> {
    return this.http.get<TrustStoreEntry[]>(url).toPromise();
  }

  uploadFile(url, props): Observable<string> {
    let input = new FormData();
    Object.keys(props).forEach(key => input.append(key, props[key]));
    return this.http.post<string>(url, input);
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

  removeCertificate(url: string, cert: any) {
    const deleteUrl = url.replace('alias', cert.name);
    return this.http.delete<string>(deleteUrl).toPromise();
  }
}
