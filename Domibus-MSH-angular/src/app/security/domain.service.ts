import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {AsyncSubject, BehaviorSubject, ReplaySubject, Subject} from 'rxjs';
import 'rxjs/add/operator/map';
import {Domain} from './domain';
import {Title} from '@angular/platform-browser';

@Injectable()
export class DomainService {

  static readonly MULTI_TENANCY_URL: string = 'rest/application/multitenancy';
  static readonly CURRENT_DOMAIN_URL: string = 'rest/security/user/domain';
  static readonly APP_DOMAIN_LIST_URL = 'rest/domains';
  static readonly USER_DOMAIN_LIST_URL = 'rest/userdomains';

  private isMultiDomainSubject: Subject<boolean>;
  private domainSubject: Subject<Domain>;
  private _domains: ReplaySubject<Domain[]>;

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

    if (this._domains) {
      this._domains.unsubscribe();
    }
    this._domains = null;
  }

  public get domains(): Observable<Domain[]> {
    if (!this._domains) {
      this._domains = new ReplaySubject<Domain[]>(1);
      this.http.get<Domain[]>(DomainService.USER_DOMAIN_LIST_URL).toPromise()
        .then(res => this._domains.next(res));
    }
    return this._domains.asObservable();
  }

  getDomains(): Promise<Domain[]> {
    let searchParams = new HttpParams();
    searchParams = searchParams.append('active', 'true');
    return this.http.get<Domain[]>(DomainService.APP_DOMAIN_LIST_URL, {params: searchParams}).toPromise();
  }

  async getAllDomains(): Promise<Domain[]> {
    const all = await this.http.get<Domain[]>(DomainService.APP_DOMAIN_LIST_URL).toPromise();
    const activeDomains = await this.getDomains();
    all.forEach(el => el.active = activeDomains.some(d => d.code == el.code));
    return all;
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

  async setActiveState(domain: Domain, active: boolean) {
    if (active) {
      await this.http.post(DomainService.APP_DOMAIN_LIST_URL, domain.code).toPromise();
      this.getDomains().then(res => this._domains.next(res));
    } else {
      await this.http.delete(DomainService.APP_DOMAIN_LIST_URL + '/' + domain.code).toPromise();
      this.getDomains().then(domains => {
        this._domains.next(domains);

        const subscr = this.getCurrentDomain().subscribe(async (current) => {
          if (current && current.code == domain.code) {
            if (domains.length) {
              this.setCurrentDomain(domains[0]);
            }
          }
        });
        subscr.unsubscribe();

      });
    }
  }
}
