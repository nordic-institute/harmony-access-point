import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/map";
import {Subject} from "rxjs";

@Injectable()
export class SecurityEventService {

  private loginSuccessSubject = new Subject<any>();
  private loginErrorSubject = new Subject<any>();
  private logoutSuccessSubject = new Subject<any>();
  private logoutErrorSubject = new Subject<any>();

  constructor() {
  }

  notifyLoginSuccessEvent() {
    this.loginSuccessSubject.next();
  }

  onLoginSuccessEvent(): Observable<any> {
    return this.loginSuccessSubject.asObservable();
  }

  notifyLoginErrorEvent(error: any) {
    this.loginErrorSubject.next(error);
  }

  onLoginErrorEvent(): Observable<any> {
    return this.loginErrorSubject.asObservable();
  }

  notifyLogoutSuccessEvent(res: any) {
    this.logoutSuccessSubject.next(res);
  }

  onLogoutSuccessEvent(): Observable<any> {
    return this.logoutSuccessSubject.asObservable();
  }

  notifyLogoutErrorEvent(error: any) {
    console.log('error logging out [' + error + ']');
    this.logoutErrorSubject.next(error);
  }

  onLogoutErrorEvent(): Observable<any> {
    return this.logoutErrorSubject.asObservable();
  }
}
