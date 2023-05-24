package eu.domibus.core.logging.cxf;

import org.apache.cxf.ext.logging.event.LogEvent;

/**
 * Business for striping the apache cxf log payloads
 *
 * @author Catalin Enache
 * @since 4.1.4
 */
public interface DomibusLoggingEventHelper {

    void stripPayload(LogEvent logEvent);

    boolean checkIfOperationIsAllowed(LogEvent logEvent);
}
