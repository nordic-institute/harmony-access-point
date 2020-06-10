package eu.domibus.api.exceptions;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Class that encapsulates information about a xsd:datetime validation exception
 */
public class DomibusDateTimeException extends DomibusCoreException {

    /**
     *
     * @param dateString the text failed to parse
     */
    public DomibusDateTimeException(String dateString) {
        super(DomibusCoreErrorCode.DOM_007, "Invalid xsd:datetime format:[" + dateString + "].");
    }

    /**
     *
     * @param dateString the text to parse, not null
     * @param cause exception to encapsulate
     */
    public DomibusDateTimeException(String dateString, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_007, "Invalid xsd:datetime format:[" + dateString + "].", cause);
    }
}
