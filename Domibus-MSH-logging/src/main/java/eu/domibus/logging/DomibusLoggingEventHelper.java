package eu.domibus.logging;

import org.apache.cxf.ext.logging.event.LogEvent;

public interface DomibusLoggingEventHelper {

    void stripPayload(LogEvent logEvent);

    boolean checkIfOperationIsAllowed(LogEvent logEvent);
}
