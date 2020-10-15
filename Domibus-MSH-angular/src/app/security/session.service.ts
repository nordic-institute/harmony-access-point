import {Injectable, OnDestroy} from '@angular/core';
import {
  LOCAL_STORAGE_KEY_CURRENT_SESSION,
  SESSION_STORAGE_KEY_EXPIRATION_SHOWN,
  SESSION_STORAGE_KEY_LOGGED_OUT,
  SessionState
} from './SessionState';
import {SessionExpiredDialogComponent} from './session-expired-dialog/session-expired-dialog.component';
import {Router} from '@angular/router';
import {DialogsService} from '../common/dialogs/dialogs.service';
import {DomibusInfoService} from '../common/appinfo/domibusinfo.service';
import {Subscription, timer} from 'rxjs';
import {SecurityEventService} from './security.event.service';

/**
 * Service providing operation required for managing sessions.
 *
 * @since 4.2
 * @author Sebastian-Ion TINCU
 * @author Ion Perpegel
 * */
@Injectable()
export class SessionService implements OnDestroy {
  extAuthProviderEnabled = false;
  private timerSubscription: Subscription;
  private loginSubscription: Subscription;

  constructor(private router: Router, private dialogsService: DialogsService,
              private domibusInfoService: DomibusInfoService, private securityEventService: SecurityEventService) {

    this.domibusInfoService.isExtAuthProviderEnabled().then(res => {
      this.extAuthProviderEnabled = res;
    });
    this.timerSubscription = timer(0, 3000)
      .subscribe(everyThreeSeconds => this.refreshUsingSessionState());
    this.loginSubscription = this.securityEventService.onLoginSuccessEvent()
      .subscribe(() => this.onLoginSuccessEvent());

  }

  resetCurrentSession() {
    localStorage.setItem(LOCAL_STORAGE_KEY_CURRENT_SESSION, JSON.stringify(SessionState.INACTIVE));
  }

  getCurrentSession(): SessionState {
    const storedSession = localStorage.getItem(LOCAL_STORAGE_KEY_CURRENT_SESSION);
    return storedSession ? JSON.parse(storedSession) : SessionState.INACTIVE;
  }

  updateCurrentSession(session: SessionState): void {
    localStorage.setItem(LOCAL_STORAGE_KEY_CURRENT_SESSION, JSON.stringify(session));
  }

  setExpiredSession(session: SessionState) {
    if (session === SessionState.EXPIRED_LOGGED_OUT || session === SessionState.EXPIRED_INACTIVITY_OR_ERROR) {
      const currentSession = this.getCurrentSession();
      // Only mark the current session as expired when the user is logged in
      if (currentSession === SessionState.ACTIVE) {
        this.updateCurrentSession(session);
        // this.showExpirationPopup();
      }
    }
  }

  private refreshUsingSessionState() {
    const session: SessionState = this.getCurrentSession();

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
      this.showExpirationPopup();
    }
  }

  private showExpirationPopup() {
    this.setExpirationDialogAlreadyShown();
    this.dialogsService.openAndThen(SessionExpiredDialogComponent, {data: this.getCurrentSession()}).then(data => {
      // refresh the router outlet since we logged out in another browser tab
      this.refreshRoute();
    });
  }

  private setExpirationDialogAlreadyShown() {
    sessionStorage.setItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN, 'true');
  }

  private isExpirationDialogAlreadyShown() {
    return sessionStorage.getItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN);
  }

  setManuallyLoggedOutInThisTab() {
    sessionStorage.setItem(SESSION_STORAGE_KEY_LOGGED_OUT, 'true');
  }

  private isManuallyLoggedOutInThisBrowserTab() {
    return sessionStorage.getItem(SESSION_STORAGE_KEY_LOGGED_OUT);
  }

  clearSessionStorage() {
    sessionStorage.removeItem(SESSION_STORAGE_KEY_EXPIRATION_SHOWN);
    sessionStorage.removeItem(SESSION_STORAGE_KEY_LOGGED_OUT);
  }

  private refreshRoute() {
    this.router.navigate([], {
      skipLocationChange: true, // don't pushing a new state into history
    });
  }

  private isExtAuthProviderEnabled(): boolean {
    return this.extAuthProviderEnabled;
  }

  private onLoginSuccessEvent() {
    this.clearSessionStorage();
  }

  ngOnDestroy() {
    this.timerSubscription.unsubscribe();
    this.loginSubscription.unsubscribe();
  }
}
