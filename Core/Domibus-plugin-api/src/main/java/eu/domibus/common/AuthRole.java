package eu.domibus.common;

/**
 * @author François Gautier
 * @since 5.0.5
 */
public enum AuthRole {
    ROLE_USER,
    ROLE_ADMIN, //lists all messages for one plugin, sends on behalf of any user, admin access to the AC
    ROLE_AP_ADMIN //admin access to all domains
}
