import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SecurityService} from './security/security.service';
import {NavigationEnd, Router, RouterOutlet, RoutesRecognized} from '@angular/router';
import {SecurityEventService} from './security/security.event.service';
import {DomainService} from './security/domain.service';
import {HttpEventService} from './common/http/http.event.service';
import {DomibusInfoService} from './common/appinfo/domibusinfo.service';
import {ApplicationContextService} from './common/application-context.service';
import {SessionExpiredDialogComponent} from './security/session-expired-dialog/session-expired-dialog.component';
import {DialogsService} from './common/dialogs/dialogs.service';
import {Server} from './security/Server';
import {
  SessionState,
  SESSION_STORAGE_KEY_LOGGED_OUT,
  SESSION_STORAGE_KEY_EXPIRATION_SHOWN
} from './security/SessionState';
import {Subscription, timer} from 'rxjs';
import {SessionService} from './security/session.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  fullMenu = true;
  menuClass: string = this.fullMenu ? 'menu-expanded' : 'menu-collapsed';
  extAuthProviderEnabled = false;
  isMultiDomain = false;
  extAuthProvideRedirectTo: string;

  @ViewChild(RouterOutlet, {static: false})
  outlet: RouterOutlet;

  private loginSubscription: Subscription;
  private timerSubscription: Subscription;

  constructor (private securityService: SecurityService,
               private router: Router,
               private securityEventService: SecurityEventService,
               private httpEventService: HttpEventService,
               private domainService: DomainService,
               private domibusInfoService: DomibusInfoService,
               private applicationService: ApplicationContextService,
               private dialogsService: DialogsService,
               private sessionService: SessionService) {

    this.domainService.setAppTitle();

    /* ugly but necessary: intercept ECAS redirect */
    this.router.events.subscribe(event => {
      if (event instanceof RoutesRecognized) {
        if (event.url.indexOf('?ticket=ST') !== -1) {
          this.onLoginSuccessEvent();
          let route = event.state.root.firstChild;
          this.extAuthProvideRedirectTo = '/' + route.url;
        }
      } else if (event instanceof NavigationEnd) {
        let comp = this.outlet && this.outlet.isActivated ? this.outlet.component : null;
        applicationService.setCurrentComponent(this.outlet.component);
      }
    });
  }

  async ngOnInit() {
    this.extAuthProviderEnabled = await this.domibusInfoService.isExtAuthProviderEnabled();
    if (this.extAuthProviderEnabled) {
      const user = await this.securityService.getCurrentUserFromServer();
      if (user) {
        this.securityService.updateCurrentUser(user);
        this.domainService.setAppTitle();
      }
      if (this.extAuthProvideRedirectTo) {
        const success = await this.router.navigate([this.extAuthProvideRedirectTo]);
        if (success) {
          console.log('redirect to: ' + this.extAuthProvideRedirectTo + ' done');
        }
      }
    }
    this.isMultiDomain = await this.domainService.isMultiDomain().toPromise();

    this.loginSubscription = this.securityEventService.onLoginSuccessEvent().subscribe(() => this.onLoginSuccessEvent());

    this.timerSubscription = timer(0, 3000).subscribe(everyThreeSeconds => this.refreshUsingSessionState());

    this.httpEventService.subscribe((error) => this.onHttpEventService(error));

    this.securityEventService.onLogoutSuccessEvent().subscribe(
      data => {
        this.router.navigate([this.isExtAuthProviderEnabled() ? '/logout' : '/login']);
      });
  }

  private refreshUsingSessionState() {
    const session: SessionState = this.sessionService.getCurrentSession();

    if (session === SessionState.ACTIVE) {
      if ((this.isExtAuthProviderEnabled() && this.router.url.match(/\/logout(\?.*)?$/))
        || (!this.isExtAuthProviderEnabled() && this.router.url.match(/\/login(\?.*)?$/))) {
        this.clearSessionStorage();

        // refresh the router outlet since we logged in another browser tab
        this.refreshRoute();
      }
    } else if (session === SessionState.EXPIRED_INACTIVITY_OR_ERROR || session === SessionState.EXPIRED_LOGGED_OUT) {
      if (this.isExpirationDialogAlreadyShown() || this.isManuallyLoggedOutInThisBrowserTab()) {
        return;
      }
      this.setExpirationDialogAlreadyShown();
      this.dialogsService.openAndThen(SessionExpiredDialogComponent, {data: session}).then(data => {
        // refresh the router outlet since we logged out in another browser tab
        this.refreshRoute();
      });
    }
  }

  private onHttpEventService(error) {
    if (error && (error.status === Server.HTTP_FORBIDDEN || error.status === Server.HTTP_UNAUTHORIZED)) {
      this.sessionService.setExpiredSession(SessionState.EXPIRED_INACTIVITY_OR_ERROR);
      this.securityService.logout();
    }
  }

  private refreshRoute() {
    this.router.navigate([], {
      skipLocationChange: true, // don't pushing a new state into history
    });
  }

  ngOnDestroy() {
    this.loginSubscription.unsubscribe();
    this.timerSubscription.unsubscribe();
  }

  onLoginSuccessEvent() {
    this.clearSessionStorage();
    this.sessionService.updateCurrentSession(SessionState.ACTIVE);
  }

  isAdmin (): boolean {
    return this.securityService.isCurrentUserAdmin();
  }

  isAdminMultiAware(): boolean {
    return (this.isMultiDomain && this.securityService.isCurrentUserSuperAdmin()) ||
      (!this.isMultiDomain && this.securityService.isCurrentUserAdmin());
  }

  isUser(): boolean {
    return this.securityService.hasCurrentUserPrivilegeUser();
  }

  isExtAuthProviderEnabled(): boolean {
    return this.extAuthProviderEnabled;
  }

  get currentUser(): string {
    const user = this.securityService.getCurrentUser();
    return user ? user.username : '';
  }

  logout(event: Event): void {
    event.preventDefault();
    this.setManuallyLoggedOutInThisTab();
    this.sessionService.setExpiredSession(SessionState.EXPIRED_LOGGED_OUT);
    this.securityService.logout();
  }

  toggleMenu() {
    this.fullMenu = !this.fullMenu
    this.menuClass = this.fullMenu ? 'menu-expanded' : 'menu-collapsed'
    setTimeout(() => {
      let evt = document.createEvent('HTMLEvents')
      evt.initEvent('resize', true, false)
      window.dispatchEvent(evt)
    }, 500)
    // ugly hack but otherwise the ng-datatable doesn't resize when collapsing the menu
    // alternatively this can be tried (https://github.com/swimlane/ngx-datatable/issues/193) but one has to implement it on every page
    // containing a ng-datatable and it only works after one clicks inside the table
  }

  changePassword() {
    this.router.navigate(['changePassword']);
  }

  private setExpirationDialogAlreadyShown() {
    sessionStorage.setItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN, 'true');
  }

  private isExpirationDialogAlreadyShown() {
    return sessionStorage.getItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN);
  }

  private setManuallyLoggedOutInThisTab() {
    sessionStorage.setItem(SESSION_STORAGE_KEY_LOGGED_OUT, 'true');
  }

  private isManuallyLoggedOutInThisBrowserTab() {
    return sessionStorage.getItem(SESSION_STORAGE_KEY_LOGGED_OUT);
  }

  private clearSessionStorage() {
    sessionStorage.removeItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN);
    sessionStorage.removeItem(SESSION_STORAGE_KEY_LOGGED_OUT);
  }
}
