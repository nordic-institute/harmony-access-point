import {Injectable} from '@angular/core';
import {LOCAL_STORAGE_KEY_CURRENT_SESSION, SessionState} from './SessionState';

/**
 * Service providing operation required for managing sessions.
 *
 * @since 4.2
 * @author Sebastian-Ion TINCU
 */
@Injectable()
export class SessionService {

  getCurrentSession (): SessionState {
    const storedSession = localStorage.getItem(LOCAL_STORAGE_KEY_CURRENT_SESSION);
    return storedSession ? JSON.parse(storedSession) : SessionState.INACTIVE;
  }

  updateCurrentSession (session: SessionState): void {
    localStorage.setItem(LOCAL_STORAGE_KEY_CURRENT_SESSION, JSON.stringify(session));
  }

  setExpiredSession (session: SessionState) {
    if (session === SessionState.EXPIRED_LOGGED_OUT || session === SessionState.EXPIRED_INACTIVITY_OR_ERROR) {
      const currentSession = this.getCurrentSession();

      // Only mark the current session as expired when the user is logged in
      if (currentSession === SessionState.ACTIVE) {
        this.updateCurrentSession(session);
      }
    }
  }
}
