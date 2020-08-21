import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/share';
import 'rxjs/add/observable/throw';
import {HttpEventService} from './http.event.service';
import {catchError} from 'rxjs/operators';
import {throwError} from 'rxjs';
import {Server} from '../../security/Server';

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
        if (error.status === Server.HTTP_FORBIDDEN) {
          console.log(`ExtendedHttpClient: received ${Server.HTTP_FORBIDDEN}`);
          this.httpEventService.requestForbiddenEvent(error);
        }
        return throwError(error);
      })
    );
  }

}
