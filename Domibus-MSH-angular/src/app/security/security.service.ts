import {Injectable, Injector} from '@angular/core';
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
import {SessionService} from './session.service';
import {SessionState} from './SessionState';
import {instanceOfModifiableList} from '../common/mixins/type.utils';

@Injectable()
export class SecurityService {
  public static ROLE_AP_ADMIN = 'ROLE_AP_ADMIN';
  public static ROLE_DOMAIN_ADMIN = 'ROLE_ADMIN';
  public static ROLE_USER = 'ROLE_USER';
  public static USER_ROLES = [SecurityService.ROLE_USER, SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];
  public static ADMIN_ROLES = [SecurityService.ROLE_DOMAIN_ADMIN, SecurityService.ROLE_AP_ADMIN];
  private static injector: Injector;

  private CURRENT_USER = 'currentUser';
  private CURRENT_USER_UPDATED_ON = 'currentUserUpdatedOn';

  pluginPasswordPolicy: Promise<PasswordPolicyRO>;
  public password: string;

  public static async getAllowedRolesForSTandMT(stRoles, mtRoles) {
    let domainService = SecurityService.injector.get<DomainService>(DomainService);
    let isMulti = await domainService.isMultiDomain().toPromise();
    return isMulti ? mtRoles : stRoles;
  }

  constructor(private http: HttpClient,
              private securityEventService: SecurityEventService,
              private alertService: AlertService,
              private domainService: DomainService,
              private applicationService: ApplicationContextService,
              private dialogsService: DialogsService,
              private propertiesService: PropertiesService,
              private sessionService: SessionService,
              private injector: Injector) {
    SecurityService.injector = this.injector;
  }

  async login(username: string, password: string): Promise<User> {
    this.domainService.resetDomain();
    this.sessionService.resetCurrentSession();

    try {
      const user = await this.http.post<User>('rest/security/authentication', {username: username, password: password}).toPromise();
      if (!user) {
        console.log('Login returned a null user!');
        throw new Error('An error occurred while logging in.');
      }
      this.initialiseApp(user);
      this.securityEventService.notifyLoginSuccessEvent(user);
      return user;
    } catch (error) {
      console.log('Login error:', error);
      this.securityEventService.notifyLoginErrorEvent(error);
      throw error;
    }
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

    const canLogout = await this.canLogout();
    if (!canLogout) {
      return;
    }

    this.sessionService.setExpiredSession(SessionState.EXPIRED_LOGGED_OUT);
    this.clearSession();

    this.http.delete('rest/security/authentication').subscribe((res) => {
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  async canLogout(): Promise<boolean> {

    const currentComponent = this.applicationService.getCurrentComponent();
    return this.canAbandonUnsavedChanges(currentComponent);
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
    localStorage.removeItem(this.CURRENT_USER);
    localStorage.removeItem(this.CURRENT_USER_UPDATED_ON);
  }

  getCurrentUser(): User {
    const storedUser = localStorage.getItem(this.CURRENT_USER);
    return storedUser ? JSON.parse(storedUser) : null;
  }

  updateCurrentUser(user: User): void {
    localStorage.setItem(this.CURRENT_USER, JSON.stringify(user));
    localStorage.setItem(this.CURRENT_USER_UPDATED_ON, new Date().toJSON());
  }

  isUserConnected(): Promise<string> {
    return this.http.get<string>('rest/security/user/connected').toPromise();
  }

  getCurrentUserFromServer(): Promise<User> {
    return this.http.get<User>('rest/security/user').toPromise();
  }

  isAuthenticated(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      // we 'ping' the server to check whether we are connected
      // if not, trigger the redirection to the login screen
      try {
        this.isUserConnected().then(isConnected => {
          resolve(true);
        }, err => {
          console.log('Error while calling isUserConnected: ' + err);
          resolve(false);
        });
      } catch (ex) {
        console.log('Error while calling isUserConnected: ' + ex);
        this.alertService.error('An error occurred while checking authentication:');
        resolve(false);
      }
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
    if (!roles) {
      return true;
    }
    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      return roles.some(role => currentUser.authorities.includes(role));
    }
    return false;
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

  async canAbandonUnsavedChanges(component: any) {

    if (!component) {
      return true;
    }

    if (!instanceOfModifiableList(component)) {
      return true;
    }

    const canBypassCheckDirty = await this.canBypassCheckDirty();
    if (canBypassCheckDirty) {
      return true;
    }

    if (!component.isDirty()) {
      return true;
    }

    return this.dialogsService.openCancelDialog();
  }

  private async canBypassCheckDirty(): Promise<boolean> {
    if (this.sessionService.getCurrentSession() !== SessionState.ACTIVE) {
      return true;
    }

    if (!this.getCurrentUser()) {
      return true;
    }

    const isAuthenticated = await this.isAuthenticated();
    if (!isAuthenticated) {
      return true;
    }
  }

  private getCurrentUserLastUpdate(): Date {
    const res = localStorage.getItem(this.CURRENT_USER_UPDATED_ON);
    return res ? new Date(res) : null;
  }


  isClientConnected() {
    const lastUpdate = this.getCurrentUserLastUpdate();
    if (lastUpdate == null) {
      return this.getCurrentUser() != null;
    }
    const diffInHours = (new Date().getTime() - this.getCurrentUserLastUpdate().getTime()) / (1000 * 60 * 60);
    const res = this.getCurrentUser() != null && diffInHours < 12;
    return res;
  }

  initialiseApp(user: User) {
    this.updateCurrentUser(user);
    this.domainService.setAppTitle();
    this.sessionService.updateCurrentSession(SessionState.ACTIVE);
  }
}

