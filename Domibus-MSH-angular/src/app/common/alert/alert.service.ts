import {Injectable, Injector} from '@angular/core';
import {
  NavigationEnd,
  NavigationStart,
  Router,
  RouterEvent
} from '@angular/router';
import {Subject} from 'rxjs/Subject';
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {instanceOfMultipleItemsResponse, MultipleItemsResponse, ResponseItemDetail} from './support/multiple-items-response';
import {MatSnackBar} from '@angular/material';
import {AlertComponent} from './alert.component';

@Injectable()
export class AlertService {
  private subject = new Subject<any>();
  private previousRoute: string;
  private needsExplicitClosing: boolean;

  // TODO move the logic in the ngInit block
  constructor(private router: Router, private matSnackBar: MatSnackBar) {
    this.previousRoute = '';
    // clear alert message on route change
    router.events.subscribe(event => this.reactToNavigationEvents(event));
  }

  public success(response: any, duration: number = 5000) {
    let message = this.formatResponse(response);
    this.matSnackBar.openFromComponent(AlertComponent, {
      data: {message: message, service: this},
      panelClass: 'success',
      duration: duration,
      verticalPosition: 'top',
    });
  }

  public exception(message: string, error: any) {
    if (error && error.handled) {
      return;
    }

    const errMsg = this.formatError(error, message);
    this.displayErrorMessage(errMsg, false, 0);
    return Promise.resolve();
  }

  public error(message: string, keepAfterNavigationChange = false, fadeTime: number = 0) {
    const errMsg = this.formatError(message);

    this.displayErrorMessage(errMsg, keepAfterNavigationChange, fadeTime);
  }

  // called from the alert component explicitly by the user
  public close(): void {
    this.matSnackBar.dismiss();
  }

  public clearAlert(): void {
    if (this.needsExplicitClosing) {
      return;
    }
    this.close();
  }

  private formatResponse(response: any | string) {
    let message = '';
    if (typeof response === 'string') {
      message = response;
    } else {
      if (instanceOfMultipleItemsResponse(response)) {
        message = this.processMultipleItemsResponse(response);
      }
    }
    return message;
  }

  private displayErrorMessage(errMsg: string, keepAfterNavigationChange: boolean, fadeTime: number) {

    this.needsExplicitClosing = keepAfterNavigationChange;
    this.matSnackBar.openFromComponent(AlertComponent, {
      data: {message: errMsg, service: this},
      panelClass: 'error',
      verticalPosition: 'top',
    });

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

  escapeHtml(unsafe: string): string {
    if (!unsafe) return '';
    if (!unsafe.replace) unsafe = unsafe.toString();
    return unsafe.replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  private formatError(error: HttpErrorResponse | HttpResponse<any> | string | any, message: string = null): string {
    let errMsg = this.tryExtractErrorMessageFromResponse(error);
    errMsg = this.tryParseHtmlResponse(errMsg);
    errMsg = this.tryClearMessage(errMsg);
    return (message ? message + ' \n' : '') + (errMsg || '');
  }

  private tryExtractErrorMessageFromResponse(response: HttpErrorResponse | HttpResponse<any> | string | any) {
    let errMsg: string = null;

    if (typeof response === 'string') {
      errMsg = response;
    } else if (response instanceof HttpErrorResponse) {
      if (response.error) {
        if (instanceOfMultipleItemsResponse(response.error)) {
          errMsg = this.processMultipleItemsResponse(response.error);
        } else if (response.error.message) {
          errMsg = this.escapeHtml(response.error.message);
        } else {
          errMsg = this.tryParseHtmlResponse(response.error);
        }
      }
    } else if (response instanceof HttpResponse) {
      errMsg = response.body;
    } else if (response instanceof Error) {
      errMsg = response.message;
    }

    return errMsg;
  }

  private processMultipleItemsResponse(response: MultipleItemsResponse) {
    let message = '';
    if (response.message) {
      message = this.escapeHtml(response.message);
    }
    if (Array.isArray(response.issues)) {
      message += '<br>' + this.formatArrayOfItems(response.issues);
    }
    return message;
  }

  private formatArrayOfItems(errors: Array<ResponseItemDetail>): string {
    let message = '';
    errors.forEach(err => {
      let m = (err.level ? err.level + ': ' : '') + (err.message ? this.escapeHtml(err.message) : '');
      message += m + '<br>';
    });
    return message;
  }

  private tryParseHtmlResponse(errMsg: string) {
    let res = errMsg;
    if (errMsg && errMsg.indexOf && errMsg.indexOf('<!doctype html>') >= 0) {
      let res0 = errMsg.match(/<p><b>Message<\/b>(.+)<\/p><p>/);
      if (res0 && res0.length > 0) {
        res = res0[1];
      }
      if(!res) {
        let res1 = errMsg.match(/<h1>(.+)<\/h1>/);
        if (res1 && res1.length > 0) {
          res = res1[1];
        }
        let res2 = errMsg.match(/<p>(.+)<\/p>/);
        if (res2 && res2.length >= 0) {
          res += res2[0];
        }
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
    if (errMsg && errMsg.toString() == '[object Object]') {
      return '';
    }
    return res;
  }

  private reactToNavigationEvents(event: RouterEvent | NavigationStart | NavigationEnd | any) {
    if (event instanceof NavigationStart) {
      if (this.isRouteChanged(event.url)) {
        this.clearAlert();
      }
    } else if (event instanceof NavigationEnd) {
      const navigationEnd: NavigationEnd = event;
      this.previousRoute = navigationEnd.url;
    }
  }

}
