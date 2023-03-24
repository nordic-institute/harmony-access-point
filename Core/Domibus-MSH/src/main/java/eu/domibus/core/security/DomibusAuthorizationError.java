package eu.domibus.core.security;

/**
 * Enum for Domibus auth errors.
 *
 * @author Ion Perpegel
 * @since 5.1
 */
public enum DomibusAuthorizationError {
    A0001("A0001", "Authorization to access the targeted application refused to sender."),
    A0002("A0002", "Connection rejected"),
    A0003("A0003", "Technical issue"),
    A0004("A0004", "System down");


    private final String code;

    private final String description;

    DomibusAuthorizationError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ":" + description + ".";
    }
}
