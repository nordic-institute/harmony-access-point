package eu.domibus.jms.wildfly;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * A runtime exception used to wrap {@code Exception}s that may be thrown in JMX scenarios for malformed object names
 * when looking up MBean objects.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
public class DomibusJMXException extends DomibusCoreException {

    public DomibusJMXException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

}
