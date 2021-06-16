package eu.domibus.plugin.ws.logging;

import org.apache.cxf.ext.logging.event.LogEvent;

/**
 * Business to strip the payload generated by apache cxf - WS Plugin
 *
 * @since 4.1.4
 * @author Catalin Enache
 */
public interface WSPluginLoggingEventHelper {

    void stripPayload(LogEvent logEvent);

    void stripHeaders(LogEvent event);

    String checkIfOperationIsAllowed(LogEvent logEvent);
}