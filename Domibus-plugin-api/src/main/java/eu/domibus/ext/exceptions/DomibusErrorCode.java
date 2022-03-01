package eu.domibus.ext.exceptions;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public enum DomibusErrorCode {

    /**
     * Generic error
     */
    DOM_001("001"),

    /**
     * Authentication error
     */
    DOM_002("002"),

    /**
     * PMode error
     */
    DOM_003("003"),

    /**
     * Parties error
     */
    DOM_004("004"),

    /**
     * Payloads error
     */
    DOM_005("005"),

    /**
     * Proxy related exception.
     */
    DOM_006("006"),
    /**
     * Invalid message exception
     */
    DOM_007("007"),
    /**
     * Convert exception
     */
    DOM_008("008"),
    /**
     * Not found exception
     */
    DOM_009("009");

    private String errorCode;

    DomibusErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
