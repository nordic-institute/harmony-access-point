package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class DomibusAlertException extends DomibusCoreException {

    public DomibusAlertException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusAlertException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusAlertException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusAlertException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomibusAlertException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
