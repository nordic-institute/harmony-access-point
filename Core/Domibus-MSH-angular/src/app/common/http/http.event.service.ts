import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';

@Injectable()
export class HttpEventService extends Subject<any> {
  constructor () {
    super();
  }

  requestForbiddenEvent (error: any) {
    if (error) {
      super.next(error);
    }
  }
}
