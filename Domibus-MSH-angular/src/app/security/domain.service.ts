import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {AsyncSubject, BehaviorSubject, Subject} from 'rxjs';
import 'rxjs/add/operator/map';
import {Domain} from './domain';
import {Title} from '@angular/platform-browser';

@Injectable()
export class DomainService {

  static readonly MULTI_TENANCY_URL: string = 'rest/application/multitenancy';
  static readonly CURRENT_DOMAIN_URL: string = 'rest/security/user/domain';
  static readonly DOMAIN_LIST_URL: string = 'rest/application/domains';

  private isMultiDomainSubject: Subject<boolean>;
  private domainSubject: Subject<Domain>;

  constructor(private http: HttpClient, private titleService: Title) {
  }

  isMultiDomain(): Observable<boolean> {
    if (!this.isMultiDomainSubject) {
      this.isMultiDomainSubject = new AsyncSubject<boolean>();
      this.http.get<boolean>(DomainService.MULTI_TENANCY_URL)
        .subscribe(res => {
          this.isMultiDomainSubject.next(res);
        }, (error: any) => {
          console.log('get isMultiDomain:' + error);
          this.isMultiDomainSubject.next(false);
        }, () => {
          this.isMultiDomainSubject.complete();
        });
    }
    return this.isMultiDomainSubject.asObservable();
  }

  getCurrentDomain(): Observable<Domain> {
    if (!this.domainSubject) {
      var subject = new BehaviorSubject<Domain>(null);
      this.http.get<Domain>(DomainService.CURRENT_DOMAIN_URL)
        .subscribe(res => {
          subject.next(res);
        }, (error: any) => {
          console.log('getCurrentDomain:' + error);
          subject.next(null);
        });
      this.domainSubject = subject;
    }
    return this.domainSubject.asObservable();
  }

  resetDomain(): void {
    if (this.domainSubject) {
      this.domainSubject.unsubscribe();
    }
    this.domainSubject = null;
  }

  getDomains(): Promise<Domain[]> {
    return this.http.get<Domain[]>(DomainService.DOMAIN_LIST_URL).toPromise();
  }

  setCurrentDomain(domain: Domain) {
    return this.http.put(DomainService.CURRENT_DOMAIN_URL, domain.code).toPromise().then(() => {
      if (this.domainSubject) {
        this.domainSubject.next(domain);
      }
    });
  }

  private getTitle(): Promise<string> {
    return this.http.get<string>('rest/application/name').toPromise();
  }

  setAppTitle() {
    this.getTitle().then((title) => {
      this.titleService.setTitle(title);
    });
  }

}
