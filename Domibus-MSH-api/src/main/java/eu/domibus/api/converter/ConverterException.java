package eu.domibus.api.converter;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Ioana Dragusanu
 * @since 4.1
 */
public class ConverterException extends DomibusCoreException {

    public ConverterException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public ConverterException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public ConverterException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public ConverterException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public ConverterException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
