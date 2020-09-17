import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import 'rxjs/add/operator/map';
import {User} from './user';
import {SecurityEventService} from './security.event.service';
import {DomainService} from './domain.service';
import {PasswordPolicyRO} from './passwordPolicyRO';
import {AlertService} from '../common/alert/alert.service';
import {ApplicationContextService} from '../common/application-context.service';
import {DialogsService} from '../common/dialogs/dialogs.service';
import {PropertiesService} from '../properties/support/properties.service';

@Injectable()
export class SecurityService {
  public static ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';
  public static ROLE_DOMAIN_ADMIN = 'ROLE_ADMIN';
  public static ROLE_USER = 'ROLE_USER';
  public static USER_ROLES = [SecurityService.ROLE_USER, SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];
  public static ADMIN_ROLES = [SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];

  // passwordPolicy: Promise<PasswordPolicyRO>;
  pluginPasswordPolicy: Promise<PasswordPolicyRO>;
  public password: string;

  constructor(private http: HttpClient,
              private securityEventService: SecurityEventService,
              private alertService: AlertService,
              private domainService: DomainService,
              private applicationService: ApplicationContextService,
              private dialogsService: DialogsService,
              private propertiesService: PropertiesService) {
  }

  login(username: string, password: string) {
    this.domainService.resetDomain();

    return this.http.post<User>('rest/security/authentication',
      {
        username: username,
        password: password
      }).subscribe((response: User) => {
        this.updateCurrentUser(response);

        this.domainService.setAppTitle();

        this.securityEventService.notifyLoginSuccessEvent(response);
      },
      (error: any) => {
        console.log('Login error');
        this.securityEventService.notifyLoginErrorEvent(error);
      });
  }

  /**
   * get the user from the server and saves it locally
   */
  async getCurrentUserAndSaveLocally() {
    let userSet = false;
    try {
      const user = await this.getCurrentUserFromServer();
      if (user) {
        this.updateCurrentUser(user);
        userSet = true;
      }
    } catch (ex) {
      console.log('getCurrentUserAndSaveLocally error' + ex);
    }
    return userSet;
  }

  async logout() {
    this.alertService.close();

    const canLogout = await this.checkCanLogout();
    if (!canLogout) {
      return;
    }

    this.clearSession();

    this.http.delete('rest/security/authentication').subscribe((res) => {
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  async checkCanLogout(): Promise<boolean> {
    const currentComponent = this.applicationService.getCurrentComponent();

    if (!currentComponent) {
      return true;
    }

    let canLogoutPromise = Promise.resolve(true);
    if (currentComponent.isDirty && currentComponent.isDirty()) {
      canLogoutPromise = this.dialogsService.openCancelDialog();
    }
    try {
      const canLogout = await canLogoutPromise;
      return canLogout;
    } catch (ex) {
      console.log('An error occurred while checking logout: ', ex);
      return true;
    }
  }

  getPluginPasswordPolicy(): Promise<PasswordPolicyRO> {
    if (!this.pluginPasswordPolicy) {
      this.pluginPasswordPolicy = this.http.get<PasswordPolicyRO>('rest/application/pluginPasswordPolicy')
        .map(this.formatValidationMessage)
        .toPromise();
    }
    return this.pluginPasswordPolicy;
  }

  private formatValidationMessage(policy: PasswordPolicyRO) {
    policy.validationMessage = policy.validationMessage.split(';').map(el => '- ' + el + '<br>').join('');
    return policy;
  }

  clearSession() {
    this.domainService.resetDomain();
    localStorage.removeItem('currentUser');
  }

  getCurrentUser(): User {
    const storedUser = localStorage.getItem('currentUser');
    return storedUser ? JSON.parse(storedUser) : null;
  }

  updateCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
  }


  getCurrentUsernameFromServer(): Promise<string> {
    return this.http.get<string>('rest/security/username').toPromise();
  }

  getCurrentUserFromServer(): Promise<User> {
    return this.http.get<User>('rest/security/user').toPromise();
  }


  isAuthenticated(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      let isAuthenticated = false;

      // we get the username from the server to trigger the redirection
      // to the login screen in case the user is not authenticated
      this.getCurrentUserFromServer().then(user => {

        isAuthenticated = user ? user.username != '' : false;
        let shouldUpdateUserLocally = isAuthenticated && !this.getCurrentUser();
        if (shouldUpdateUserLocally) {
          this.updateCurrentUser(user);
        }
        resolve(isAuthenticated);
      }).catch(reason => {
        console.log('Error while calling getCurrentUsernameFromServer: ' + reason);
        reject(reason);
      });
    });
  }

  isCurrentUserSuperAdmin(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_AP_ADMIN]);
  }

  isCurrentUserAdmin(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN]);
  }

  hasCurrentUserPrivilegeUser(): boolean {
    return this.isCurrentUserInRole([SecurityService.ROLE_USER, SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN]);
  }

  isUserFromExternalAuthProvider(): boolean {
    const user = this.getCurrentUser();
    return user ? user.externalAuthProvider : false;
  }

  isCurrentUserInRole(roles: Array<string>): boolean {
    let hasRole = false;
    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      roles.forEach((role: string) => {
        if (currentUser.authorities.indexOf(role) !== -1) {
          hasRole = true;
        }
      });
    }
    return hasRole;
  }

  isAuthorized(roles: Array<string>) {
    let isAuthorized = false;
    if (roles) {
      isAuthorized = this.isCurrentUserInRole(roles);
    }
    return isAuthorized;
  }

  getPasswordPolicy(forDomain: boolean = true): Promise<PasswordPolicyRO> {
    return this.http.get<PasswordPolicyRO>('rest/application/passwordPolicy?forDomain=' + forDomain)
      .map(this.formatValidationMessage)
      .toPromise();
  }

  mustChangePassword(): boolean {
    return this.isDefaultPasswordUsed();
  }

  private isDefaultPasswordUsed(): boolean {
    const currentUser: User = this.getCurrentUser();
    return currentUser && currentUser.defaultPasswordUsed;
  }

  shouldChangePassword(): any {
    if (this.isDefaultPasswordUsed()) {
      return {
        response: true,
        reason: 'You are using the default password. Please change it now in order to be able to use the console.',
        redirectUrl: 'changePassword'
      };
    }

    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.daysTillExpiration !== null) {
      let interval: string = 'in ' + currentUser.daysTillExpiration + ' day(s)';
      if (currentUser.daysTillExpiration === 0) {
        interval = 'today';
      }
      return {
        response: true,
        reason: 'The password is about to expire ' + interval + '. We recommend changing it.',
        redirectUrl: 'changePassword'
      };
    }
    return {response: false};

  }

  async changePassword(params): Promise<any> {
    const res = this.http.put('rest/security/user/password', params).toPromise();
    await res;

    const currentUser = this.getCurrentUser();
    currentUser.defaultPasswordUsed = false;
    this.updateCurrentUser(currentUser);

    return res;
  }

  async getPasswordPolicyForUserRole(role: string): Promise<PasswordPolicyRO> {
    const forDomain = role !== SecurityService.ROLE_AP_ADMIN;
    const pattern = await this.propertiesService.getDomainOrGlobalPropertyValue('domibus.passwordPolicy.pattern', forDomain);
    const message = await this.propertiesService.getDomainOrGlobalPropertyValue('domibus.passwordPolicy.validationMessage', forDomain);
    return new PasswordPolicyRO(pattern, message);
  }
}

