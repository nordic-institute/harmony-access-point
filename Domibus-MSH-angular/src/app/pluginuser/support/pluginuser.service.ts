import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {PluginUserRO} from './pluginuser';
import {UserState} from '../../user/support/user';
import {UserService} from '../../user/support/user.service';
import {SecurityService} from '../../security/security.service';
import {AlertService} from '../../common/alert/alert.service';
import {DomainService} from '../../security/domain.service';

@Injectable()
export class PluginUserService {

  static readonly PLUGIN_USERS_URL: string = 'rest/plugin/users';

  public static passwordPattern = '^(?=.*[A-Z])(?=.*[ !#$%&\'()*+,-./:;<=>?@\\[^_`{|}~\\\]"])(?=.*[0-9])(?=.*[a-z]).{8,32}$';

  public static originalUserPattern = 'urn:oasis:names:tc:ebcore:partyid\\-type:[a-zA-Z0-9_:-]+:[a-zA-Z0-9_:-]+';
  public static originalUserMessage = 'You should follow the rule: urn:oasis:names:tc:ebcore:partyid-type:[unregistered]:[corner]';

  public static certificateIdPattern = 'CN=[a-zA-Z0-9_]+,O=[a-zA-Z0-9_]+,C=[a-zA-Z]{2}:[a-zA-Z0-9]+';
  public static certificateIdMessage = 'You should follow the rule CN=[name],O=[name],C=[country code]:[id]';

  public static CSV_URL = 'rest/plugin/csv';

  readonly ROLE_AP_ADMIN = SecurityService.ROLE_AP_ADMIN;

  constructor(private http: HttpClient, private userService: UserService, private alertService: AlertService,
              private domainService: DomainService) {
  }

  createNew(): PluginUserRO {
    const item = new PluginUserRO();
    item.status = UserState[UserState.NEW];
    item.userName = '';
    item.active = true;
    item.suspended = false;
    return item;
  }

  saveUsers(users: PluginUserRO[]): Promise<any> {
    users = users.filter(el => el.status !== UserState[UserState.PERSISTED]);
    return this.http.put(PluginUserService.PLUGIN_USERS_URL, users).toPromise();
  }

  getUserRoles(): Observable<String[]> {
    return this.userService.getUserRoles().map(items => items.filter(item => item !== this.ROLE_AP_ADMIN));
  }

  async checkConfiguredCorrectlyForMultitenancy(users: PluginUserRO[]) {
    const isMultiDomain = await this.domainService.isMultiDomain().toPromise();
    if (isMultiDomain) {
      const usersWithoutDomain = users.filter(user => !user.domain);
      if (usersWithoutDomain.length > 0) {
        const userNames = usersWithoutDomain.map(u => u.userName || u.certificateId).join(', ');
        this.alertService.error(`The following plugin users are not configured correctly for multiteancy: ${userNames}`);
      }
    }
  }

}

export class PluginUserSearchCriteria {
  authType: string;
  authRole: string;
  userName: string;
  originalUser: string;
}
