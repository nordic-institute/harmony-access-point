import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {AlertService} from './alert/alert.service';
import {HttpResponse} from '@angular/common/http';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor (private injector: Injector) {
  }

  handleError (error: HttpResponse<any> | any) {

    console.error(error);

    // if (error == 'ExpressionChangedAfterItHasBeenCheckedError') {}

    if (error instanceof HttpResponse) {
      const res = <HttpResponse<any>> error;
      if (res.status === 401 || res.status === 403) return;
    }

    const alertService = this.injector.get(AlertService);
    alertService.error(error);
  }

}
