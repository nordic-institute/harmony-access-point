import {Component, OnInit, ViewChild} from '@angular/core';
import {SecurityService} from './security/security.service';
import {NavigationEnd, Router, RouterOutlet, RoutesRecognized} from '@angular/router';
import {SecurityEventService} from './security/security.event.service';
import {DomainService} from './security/domain.service';
import {HttpEventService} from './common/http/http.event.service';
import {DomibusInfoService} from './common/appinfo/domibusinfo.service';
import {ApplicationContextService} from './common/application-context.service';
import {DialogsService} from './common/dialogs/dialogs.service';
import {Server} from './security/Server';
import {SessionState} from './security/SessionState';
import {SessionService} from './security/session.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  fullMenu = true;
  menuClass: string = this.fullMenu ? 'menu-expanded' : 'menu-collapsed';
  extAuthProviderEnabled = false;
  isMultiDomain = false;
  extAuthProvideRedirectTo: string;

  @ViewChild(RouterOutlet, {static: false})
  outlet: RouterOutlet;

  constructor(private securityService: SecurityService,
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
          this.sessionService.clearSessionStorage();
          this.sessionService.updateCurrentSession(SessionState.ACTIVE);
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
    const getUserFn = () => this.securityService.getCurrentUserFromServer();
    await this.securityService.initialiseApp(getUserFn);

    this.extAuthProviderEnabled = await this.domibusInfoService.isExtAuthProviderEnabled();
    if (this.extAuthProviderEnabled && this.extAuthProvideRedirectTo) {
      const success = await this.router.navigate([this.extAuthProvideRedirectTo]);
      if (success) {
        console.log('redirect to: ' + this.extAuthProvideRedirectTo + ' done');
      }
    }

    this.isMultiDomain = await this.domainService.isMultiDomain().toPromise();

    this.httpEventService.subscribe((error) => this.onHttpEventService(error));

    this.securityEventService.onLogoutSuccessEvent().subscribe(
      data => {
        this.router.navigate([this.isExtAuthProviderEnabled() ? '/logout' : '/login']);
      });
  }

  isAdmin(): boolean {
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

  async logout(event: Event) {
    event.preventDefault();
    await this.securityService.logout();
    this.sessionService.setManuallyLoggedOutInThisTab();
  }

  toggleMenu() {
    this.fullMenu = !this.fullMenu
    this.menuClass = this.fullMenu ? 'menu-expanded' : 'menu-collapsed';

    // ugly hack but otherwise the ng-datatable doesn't resize when collapsing the menu
    setTimeout(() => {
      let evt = document.createEvent('HTMLEvents');
      evt.initEvent('resize', true, false);
      window.dispatchEvent(evt);
    }, 100);
  }

  changePassword() {
    this.router.navigate(['changePassword']);
  }

  private onHttpEventService(error) {
    // TODO(18/09/20, Ion Perpegel): review the possible status values and their meaning
    if (error && (error.status === Server.HTTP_FORBIDDEN || error.status === Server.HTTP_UNAUTHORIZED)) {
      // did we have previously a valid session?
      if (this.securityService.isClientConnected()) {
        this.sessionService.setExpiredSession(SessionState.EXPIRED_INACTIVITY_OR_ERROR);
        this.securityService.logout();
      }
    }
  }
}
