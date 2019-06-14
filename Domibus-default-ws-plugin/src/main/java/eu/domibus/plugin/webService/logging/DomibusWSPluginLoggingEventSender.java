package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLoggingEventSender;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.LogEvent;

/**
 * This class extends {@code DomibusLoggingEventSender}
 * <p>
 * It will implement operations based on {@code LogEvent} like partially printing the payload, etc
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class DomibusWSPluginLoggingEventSender extends DomibusLoggingEventSender {

    static final String RETRIEVE_MESSAGE_RESPONSE = "retrieveMessageResponse";
    static final String VALUE_START = "<value>";
    static final String VALUE_END = "</value>";
    static final String SUBMIT_MESSAGE = "submitRequest";

    /**
     * It removes some parts of the payload info
     *
     * @param event
     */
    @Override
    protected void stripPayload(LogEvent event) {
        final String payload = event.getPayload();

        if (payload.contains(RETRIEVE_MESSAGE_RESPONSE) || payload.contains(SUBMIT_MESSAGE)) {
            //C4 - C3
            int indexStart = payload.indexOf(VALUE_START);
            int indexEnd = payload.indexOf(VALUE_END);
            event.setPayload(payload.replace(payload.substring(indexStart + VALUE_START.length(), indexEnd),
                    AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }
    }

}
