import {UserResponseRO} from './user';
import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {AlertService} from '../../common/alert/alert.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {SecurityService} from '../../security/security.service';
import {DomainService} from '../../security/domain.service';

@Injectable()
export class UserService {

  constructor(private http: HttpClient,
              private alertService: AlertService,
              private securityService: SecurityService,
              private domainService: DomainService) {
  }

  getUsers(criteria: UserSearchCriteria): Promise<UserResponseRO[]> {
    return this.http.get<UserResponseRO[]>('rest/user/users', {params: this.buildSearchParams(criteria)}).toPromise();
  }

  getUserNames(): Observable<string[]> {
    const criteria = new UserSearchCriteria();
    criteria.deleted_notSet = true;
    return this.http.get<UserResponseRO[]>('rest/user/users', {params: this.buildSearchParams(criteria)})
      .pipe(map((users: UserResponseRO[]) => users.map(u => u.userName)));
  }

  getUserRoles(): Observable<string[]> {
    return this.http.get<string[]>('rest/user/userroles')
  }

  deleteUsers(users: Array<UserResponseRO>): void {
    this.http.post('rest/user/delete', users).subscribe(res => {
      this.alertService.success('User(s) deleted');
    }, err => {
      this.alertService.exception('Error deleting user: ', err);
    });
  }

  async isDomainVisible(): Promise<boolean> {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    return isMultiDomain && this.securityService.isCurrentUserSuperAdmin();
  }

  async checkConfiguredCorrectlyForMultitenancy(users: UserResponseRO[]) {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    if (isMultiDomain) {
      const usersWithoutDomain = users.filter(user => !user.deleted && !user.domain);
      if (usersWithoutDomain.length > 0) {
        const userNames = usersWithoutDomain.map(u => u.userName).join(', ');
        this.alertService.error(`The following users are not configured correctly for multitenancy: ${userNames}`);
      }
    }
  }

  private buildSearchParams(criteria: UserSearchCriteria): HttpParams {
    const {
      authRole,
      userName,
      pageStart,
      pageSize
    } = criteria;

    let params = new HttpParams()
        .set('pageStart', String(pageStart))
        .set('pageSize', String(pageSize));

    const deleted = criteria.deleted_notSet ? 'all' : (criteria.deleted === true).toString();
    params = params.set('deleted', deleted);

    if (authRole) {
      params = params.set('authRole', authRole);
    }

    if (userName) {
      params = params.set('userName', userName);
    }

    return params;
  }

}

export class UserSearchCriteria {
  authRole: string;
  userName: string;
  deleted: boolean = false;
  deleted_notSet: boolean = false;
  pageStart: number = 0;
  pageSize: number = -1;
  i: number = 0;
}
