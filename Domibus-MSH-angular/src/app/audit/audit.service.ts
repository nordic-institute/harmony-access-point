import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpClient, HttpParams} from '@angular/common/http';
import 'rxjs-compat/add/operator/mergeMap';
import 'rxjs-compat/add/observable/from';

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * In charge of retrieving audit information from the backend.
 */
@Injectable()
export class AuditService {

  constructor(private http: HttpClient) {
  }

  countAuditLogs(searchParams: HttpParams): Observable<number> {
    return this.http.get<number>('rest/audit/count', {params: searchParams});
  }

  listTargetTypes(): Observable<string[]> {
    return this.http.get<string[]>('rest/audit/targets');
  }

  listActions(): Observable<string> {
    return Observable.from(['Created', 'Modified', 'Deleted', 'Downloaded', 'Resent', 'Moved']);
  }

}
