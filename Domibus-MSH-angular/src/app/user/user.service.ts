import {UserResponseRO} from './user';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {Observable} from 'rxjs/Observable';
import {SecurityService} from '../security/security.service';
import {DomainService} from '../security/domain.service';

@Injectable()
export class UserService {

  constructor(private http: HttpClient,
              private alertService: AlertService,
              private securityService: SecurityService,
              private domainService: DomainService) {
  }

  getUsers(filter: UserSearchCriteria): Observable<UserResponseRO[]> {
    return this.http.get<UserResponseRO[]>('rest/user/users')
      .filter(this.filterData(filter))
      .catch(err => this.alertService.handleError(err));
  }

  getUserNames(): Observable<string[]> {
    return this.http.get<UserResponseRO[]>('rest/user/users')
      .map((users: UserResponseRO[]) => users.map(u => u.userName))
      .catch(err => this.alertService.handleError(err));
  }

  getUserRoles(): Observable<string[]> {
    return this.http.get<string[]>('rest/user/userroles')
      .catch(err => this.alertService.handleError(err));
  }

  deleteUsers(users: Array<UserResponseRO>): void {
    this.http.post('rest/user/delete', users).subscribe(res => {
      this.alertService.success('User(s) deleted', false);
    }, err => {
      this.alertService.error(err, false);
    });
  }

  async isDomainVisible(): Promise<boolean> {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    return isMultiDomain && this.securityService.isCurrentUserSuperAdmin();
  }

  private filterData(filter: UserSearchCriteria) {
    return function (users) {
      let results = users.slice();
      if (filter.deleted != null) {
        results = users.filter(el => el.deleted === filter.deleted)
      }
      users.length = 0;
      users.push(...results);
      return users;
    }
  }

}

export class UserSearchCriteria {
  authRole: string;
  userName: string;
  deleted: boolean;
}

