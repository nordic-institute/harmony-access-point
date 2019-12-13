import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/share';
import 'rxjs/add/observable/throw';
import {HttpEventService} from './http.event.service';
import {catchError} from 'rxjs/operators';

@Injectable()
export class ExtendedHttpInterceptor implements HttpInterceptor {
  http: HttpClient;
  httpEventService: HttpEventService;

  constructor(httpEventService: HttpEventService) {
    this.httpEventService = httpEventService;
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if ((error.status === 403)) {
          console.log('ExtendedHttpClient: received 403');
          this.httpEventService.requestForbiddenEvent(error);
        }
        return Observable.throw(error);
      })
    );
  }

}
