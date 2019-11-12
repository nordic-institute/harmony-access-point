import {ErrorHandler, Injectable, Injector} from '@angular/core';
import {AlertService} from './alert/alert.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

  constructor (private injector: Injector) {
  }

  handleError (error: Response | any) {

    console.error(error);

    if (error instanceof Response) {
      const res = <Response> error;
      if (res.status === 401 || res.status === 403) return;
    }

    const alertService = this.injector.get(AlertService);
    alertService.error(error);
  }

}
