package eu.domibus.api.exceptions;

/**
 * @author François Gautier
 * @since 4.2
 * <p>
 * Class that encapsulates information about a xsd:datetime validation exception
 */
public class DomibusDateTimeException extends DomibusCoreException {


    public DomibusDateTimeException(DomibusCoreErrorCode coreErrorCode, String msg) {
        super(coreErrorCode, msg);
    }
    /**
     * @param dateString the text failed to parse
     */
    public DomibusDateTimeException(String dateString) {
        super(DomibusCoreErrorCode.DOM_007, "Invalid xsd:datetime format:[" + dateString + "].");
    }

    /**
     * @param dateString the text failed to parse
     */
    public DomibusDateTimeException(String dateString, String formatter) {
        super(DomibusCoreErrorCode.DOM_007, "Invalid xsd:datetime format:[" + dateString + "] with [" + formatter + "].");
    }

    /**
     * @param dateString the text to parse, not null
     * @param cause      exception to encapsulate
     */
    public DomibusDateTimeException(String dateString, String formatter, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_007, "Invalid xsd:datetime format:[" + dateString + "] with [" + formatter + "]. Caused by: [" + cause.getMessage() + "]");
    }
}
