/**
 * Session state with their possible expiration reason messages.
 *
 * @since 4.2
 * @author Sebastian-Ion TINCU
 */
export const enum SessionState {
  INACTIVE,
  ACTIVE,
  EXPIRED_INACTIVITY_OR_ERROR = 'You have been logged out because of inactivity or missing access permissions.',
  EXPIRED_LOGGED_OUT = 'You have been logged out in another tab.'
}

/**
 * Session storage key for the "logged out" object which indicates we have logged out in this browser tab, being used
 * to notify Domibus applications running in other browser tabs.
 */
export const SESSION_STORAGE_KEY_LOGGED_OUT = 'loggedOut';

/**
 * Session storage key for the "logged out" object which indicates we have logged out in this browser tab, being used
 * to notify Domibus applications running in other browser tabs.
 */
export const SESSION_STORAGE_KEY_EXPIRATION_SHOWN = 'expirationShown';


/**
 * Local storage key for the current session state object used identify possible session expiration reasons.
 */
export const LOCAL_STORAGE_KEY_CURRENT_SESSION = 'currentSession';
