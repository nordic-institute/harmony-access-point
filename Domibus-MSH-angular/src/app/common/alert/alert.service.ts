import {Injectable} from '@angular/core';
import {NavigationEnd, NavigationStart, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {Subject} from 'rxjs/Subject';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';

@Injectable()
export class AlertService {
  private subject = new Subject<any>();
  private previousRoute: string;
  private needsExplicitClosing: boolean;

  // TODO move the logic in the ngInit block
  constructor(private router: Router) {
    this.previousRoute = '';
    // clear alert message on route change
    router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        if (this.isRouteChanged(event.url)) {
          // console.log('Clearing alert when navigating from [' + this.previousRoute + '] to [' + event.url + ']');
          if (!this.needsExplicitClosing) {
            this.clearAlert();
          }
        } else {
          // console.log('Alert kept when navigating from [' + this.previousRoute + '] to [' + event.url + ']');
        }
      } else if (event instanceof NavigationEnd) {
        const navigationEnd: NavigationEnd = event;
        this.previousRoute = navigationEnd.url;
      }
    });
  }

  // called from the alert component explicitly by the user
  public close(): void {
    this.subject.next();
  }

  public clearAlert(): void {
    if (this.needsExplicitClosing) {
      return;
    }
    this.close();
  }

  public success(message: string, keepAfterNavigationChange = false) {
    this.needsExplicitClosing = keepAfterNavigationChange;
    this.subject.next({type: 'success', text: message});
  }

  public exception(message: string, error: any, keepAfterNavigationChange = false, fadeTime: number = 0) {
    const errMsg = this.formatError(error, message);
    this.displayMessage(errMsg, keepAfterNavigationChange, fadeTime);
  }

  public error(message: HttpResponse<any> | string | any, keepAfterNavigationChange = false,
               fadeTime: number = 0) {
    if (message.handled) return;
    if ((message instanceof HttpResponse) && (message.status === 401 || message.status === 403)) return;
    if (message.toString().indexOf('Response with status: 403 Forbidden') >= 0) return;

    const errMsg = this.formatError(message);

    this.displayMessage(errMsg, keepAfterNavigationChange, fadeTime);
  }

  public getMessage(): Observable<any> {
    return this.subject.asObservable();
  }

  public handleError(error: HttpResponse<any> | any) {
    this.error(error, false);

    let errMsg: string;
    if (error instanceof HttpResponse) {
      const body = error.headers && error.headers.get('content-type') !== 'text/html;charset=utf-8' ? error.body || '' : error.toString();
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.error(errMsg);
    return Promise.reject({reason: errMsg, handled: true});
  }

  private displayMessage(errMsg: string, keepAfterNavigationChange: boolean, fadeTime: number) {
    this.needsExplicitClosing = keepAfterNavigationChange;
    this.subject.next({type: 'error', text: errMsg});
    if (fadeTime) {
      setTimeout(() => this.clearAlert(), fadeTime);
    }
  }

  private getPath(url: string): string {
    var parser = document.createElement('a');
    parser.href = url;
    return parser.pathname;
  }

  private isRouteChanged(currentRoute: string): boolean {
    let result = false;
    const previousRoutePath = this.getPath(this.previousRoute);
    const currentRoutePath = this.getPath(currentRoute);
    if (previousRoutePath !== currentRoutePath) {
      result = true;
    }
    return result;
  }

  private formatError(error: HttpErrorResponse | HttpResponse<any> | string | any, message: string = null): string {
    let errMsg = this.tryExtractErrorMessageFromResponse(error);

    errMsg = this.tryParseHtmlResponse(errMsg);

    errMsg = this.tryClearMessage(errMsg);

    return (message ? message + ' \n' : '') + (errMsg || '');
  }

  private tryExtractErrorMessageFromResponse(error: HttpErrorResponse | HttpResponse<any> | string | any) {
    let errMsg: string = null;

    if (typeof error === 'string') {
      errMsg = error;
    } else if (error instanceof HttpErrorResponse) {
      if (error.error && error.error.message) {
        errMsg = error.error.message;
      } else {
        errMsg = error.error;
      }
    } else if (error instanceof HttpResponse) {
      errMsg = error.body;
    }

    //TODO: check if it is dead code with the new Http library
    if (!errMsg) {
      try {
        if (error.headers && error.headers.get('content-type') !== 'text/html;charset=utf-8' && error.json) {
          if (error.hasOwnProperty('message')) {
            errMsg = error.message;
          } else {
            errMsg = error.toString();
          }
        } else {
          errMsg = error._body ? error._body : error.toString();
        }
      } catch (e) {
      }
    }

    return errMsg;
  }

  private tryParseHtmlResponse(errMsg: string) {
    let res = errMsg;
    if (errMsg.indexOf('<!doctype html>') >= 0) {
      let res1 = errMsg.match(/<h1>(.+)<\/h1>/);
      if (res1 && res1.length > 0) {
        res = res1[1];
      }
      let res2 = errMsg.match(/<p>(.+)<\/p>/);
      if (res2 && res2.length >= 0) {
        res += res2[0];
      }
    }
    return res;
  }

  private tryClearMessage(errMsg: string) {
    let res = errMsg;
    if (errMsg && errMsg.replace) {
      res = errMsg.replace('Uncaught (in promise):', '');
      res = res.replace('[object ProgressEvent]', '');
    }
    return res;
  }
}
