import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {AlertService} from './alert/alert.service';
import {HttpResponse} from '@angular/common/http';
import {Server} from '../security/Server';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor(private injector: Injector) {
  }

  handleError(error: HttpResponse<any> | any) {
    console.error(error);

    if (error instanceof HttpResponse) {
      const res = <HttpResponse<any>>error;
      if (res.status === Server.HTTP_UNAUTHORIZED || res.status === Server.HTTP_FORBIDDEN) {
        return;
      }
    } else if (error.rejection) {
      // unpack the promise rejection
      error = error.rejection;
    }
    const alertService = this.injector.get(AlertService);
    alertService.exception('', error);
  }

}
