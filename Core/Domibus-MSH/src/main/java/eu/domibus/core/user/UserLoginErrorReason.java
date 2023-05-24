package eu.domibus.core.user;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum UserLoginErrorReason {
    UNKNOWN,
    INACTIVE,
    SUSPENDED,
    BAD_CREDENTIALS,
    PASSWORD_EXPIRED,
}
