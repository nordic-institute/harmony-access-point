package eu.domibus.api.user;

/**
 * Raised when there is no active admin left for a domain
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public class AtLeastOneAdminException extends UserManagementException {
    public AtLeastOneAdminException() {
        super("There must always be at least one active Domain Admin for each Domain.");
    }
}
