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
import {Router} from '@angular/router';
import {DefaultPasswordDialogComponent} from './default-password-dialog/default-password-dialog.component';

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

  private initialiseAppPromise: Promise<User>;

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
              private injector: Injector,
              private router: Router) {
    SecurityService.injector = this.injector;
  }

  async login(username: string, password: string, returnUrl: string) {
    try {
      this.domainService.resetDomain();
      const getUserFn = () => this.doLogin(username, password);
      await this.initialiseApp(getUserFn);
      this.sessionService.clearSessionStorage();
      this.handleDefaultPassword(password, returnUrl);
    } catch (error) {
      console.log('Login error:', error);
      this.securityEventService.notifyLoginErrorEvent(error);
      throw error;
    }
  }

  private handleDefaultPassword(password: string, returnUrl: string) {
    const changePassword = this.shouldChangePassword();
    if (changePassword.response === true) {
      this.password = password;
      this.dialogsService.open(DefaultPasswordDialogComponent, {data: changePassword.reason});
      this.router.navigate([changePassword.redirectUrl]);
    } else {
      this.router.navigate([returnUrl]);
      this.sessionService.checkSessionState();
    }
  }

  private async doLogin(username: string, password: string): Promise<User> {
    const user = await this.http.post<User>('rest/security/authentication', {
      username: username,
      password: password
    }).toPromise();
    if (!user) {
      throw new Error('An error occurred while logging in.');
    }
    return user;
  }

  async logout(): Promise<any> {
    const canLogout = await this.canLogout();
    if (!canLogout) {
      return;
    }

    this.clearAppData(SessionState.EXPIRED_LOGGED_OUT);
    return this.http.delete('rest/security/authentication').subscribe((res) => {
        console.log('logged out')
        this.securityEventService.notifyLogoutSuccessEvent(res);
      },
      (error: any) => {
        this.securityEventService.notifyLogoutErrorEvent(error);
      });
  }

  clearAppData(sessionState: SessionState) {
    // did we have a valid session previously?
    if (this.hasUser()) {
      this.alertService.close();
      this.dialogsService.closeActive();
      this.sessionService.setExpiredSession(sessionState);
      this.clearSession();
      this.domainService.resetDomain();
    }
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

  private clearSession() {
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

  private isUserConnected(): Promise<string> {
    return this.http.get<string>('rest/security/user/connected').toPromise();
  }

  getCurrentUserFromServer(): Promise<User> {
    return this.http.get<User>('rest/security/user').toPromise();
  }

  async isAuthenticated(): Promise<boolean> {
    // we 'ping' the server to check whether we are connected
    try {
      await this.isUserConnected();
      return true;
    } catch (err) {
      console.log('Error while calling isUserConnected: ', err);
      return false;
    }
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

  isCurrentUserInRole(roles: Array<string>, logWarning = false): boolean {
    if (!roles) {
      return true;
    }
    const currentUser = this.getCurrentUser();
    if (currentUser && currentUser.authorities) {
      let result = roles.some(role => currentUser.authorities.includes(role));
      if (!result && logWarning) {
        console.warn(`Authorization check failed for user [${currentUser?.username}] with authorities [${currentUser?.authorities}]. None of [${roles}] found.`);
      }
      return result;
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

  isDefaultPasswordUsed(): boolean {
    const currentUser: User = this.getCurrentUser();
    return currentUser && currentUser.defaultPasswordUsed;
  }

  private shouldChangePassword(): any {
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

    this.sessionService.checkSessionState();

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

    const canBypassCheckDirty = await this.canBypassDirtyChecking();
    if (canBypassCheckDirty) {
      return true;
    }

    if (!component.isDirty()) {
      return true;
    }

    return this.dialogsService.openCancelDialog();
  }

  private async canBypassDirtyChecking(): Promise<boolean> {
    if (this.sessionService.getCurrentSession() !== SessionState.ACTIVE) {
      return true;
    }

    if (!this.getCurrentUser()) {
      return true;
    }

    let isAuthenticated = false;
    try {
      isAuthenticated = await this.isAuthenticated();
    } catch (ex) {
      console.log('Error calling isAuthenticated()')
    }
    if (!isAuthenticated) {
      return true;
    }
  }

  private hasUser(): boolean {
    return this.getCurrentUser() != null;
  }

  async isAppInitialized(): Promise<boolean> {
    if (this.initialiseAppPromise) {
      try {
        await this.initialiseAppPromise;
        return true;
      } catch (e) {
        this.alertService.exception('Exception while initializing application', e);
        return false;
      }
    }
    return false;
  }

  initialiseApp(getUserFn: () => Promise<User>): Promise<User> {
    this.initialiseAppPromise = new Promise((resolve, reject) => {
      this.sessionService.resetCurrentSession();
      this.clearSession();
      getUserFn().then((user: User) => {
        if (user) {
          this.updateCurrentUser(user);
          this.domainService.setAppTitle();
          this.sessionService.updateCurrentSession(SessionState.ACTIVE);
        } else {
          console.warn(getUserFn + ' method returned an empty user.');
        }
        resolve(user);
      }, err => {
        console.log('Error while calling ' + getUserFn.name + ' function: ', err);
        reject(err);
      });
    });

    return this.initialiseAppPromise;
  }

}

