package eu.domibus.logging;

import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business for striping the apache cxf log payloads
 *
 * @author Catalin Enache
 * @since 4.1.4
 */
@Service
public class DomibusLoggingEventHelperImpl implements DomibusLoggingEventHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingEventHelperImpl.class);

    static final String CONTENT_TYPE_MARKER = "Content-Type:";

    @Override
    public void stripPayload(LogEvent logEvent) {
        final String operationName = logEvent.getOperationName();
        final EventType type = logEvent.getType();
        final boolean isMultipart = logEvent.isMultipartContent();
        LOG.debug("operationName=[{}] eventType=[{}] multipart=[{}]", operationName, type, isMultipart);

        if (checkIfOperationIsAllowed(logEvent)) {
            String payload = logEvent.getPayload();
            logEvent.setPayload(replacePayloadMSH(payload));
        }
    }

    @Override
    public boolean checkIfOperationIsAllowed(LogEvent logEvent) {
        //if it's multipart and direction C2 -> C3 we will strip the AS4 payload
        return logEvent.isMultipartContent() &&
                EventType.REQ_OUT == logEvent.getType();
    }


    /**
     * Replaces the payload for MSH invocation C2 -> C3
     *
     * @param payload
     * @return
     */
    private String replacePayloadMSH(final String payload) {
        String newPayload = payload;

        String[] payloadSplits = payload.split(CONTENT_TYPE_MARKER);
        //keeping only first 2 Content-Type elements
        if (payloadSplits.length >= 2) {
            newPayload = payloadSplits[0] + CONTENT_TYPE_MARKER + payloadSplits[1] + AbstractLoggingInterceptor.CONTENT_SUPPRESSED;
        }
        return newPayload;
    }


    private String replaceInPayload(final String payload) {
        String newPayload = payload;
        //C2 -> C3
        String[] payloadSplits = payload.split(CONTENT_TYPE_MARKER);
        //keeping only first 2 Content-Type elements
        if (payloadSplits.length >= 2) {
            newPayload = payloadSplits[0] + CONTENT_TYPE_MARKER + payloadSplits[1] + AbstractLoggingInterceptor.CONTENT_SUPPRESSED;
        }
        return newPayload;
    }


}
