package eu.domibus.logging;

import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.EventType;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends the default {@code Slf4jEventSender} implemented by Apache CXF
 * <p>
 * It will implement operations based on {@code LogEvent} like partially printing the payload, etc
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class DomibusLoggingEventSender extends Slf4jEventSender implements LogEventSender {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingEventSender.class);

    protected static final String CONTENT_TYPE_MARKER = "Content-Type:";
    private static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";

    private boolean printPayload;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Override
    protected String getLogMessage(LogEvent event) {
        if (checkIfStripPayloadPossible()) {
            try {
                stripPayload(event);
            } catch (RuntimeException e) {
                LOG.error("Exception while stripping the payload: ", e);
            }
        }

        return LogMessageFormatter.format(event);
    }

    /**
     * It removes some parts of the payload info
     *
     * @param event
     */
    protected void stripPayload(LogEvent event) {
        final String operationName = event.getOperationName();
        final EventType eventType = event.getType();

        //check conditions to strip the payload and get the xmlTag
        final String xmlTag = DomibusLoggingEventStripPayloadEnum.getXmlNodeIfStripPayloadIsPossible(operationName, eventType);
        if (xmlTag == null) {
            LOG.debug("for operationName=[{}] and eventType=[{}] we don't strip the payload", operationName, eventType);
            return;
        }

        //if it's multipart and we want to strip the AS4 payload
        if (event.isMultipartContent()) {
            String payload = event.getPayload();
            event.setPayload(replaceInPayload(payload, xmlTag));
        }

    }

    private boolean checkIfStripPayloadPossible() {
        LOG.debug("printPayload=[{}]", printPayload);
        if (printPayload) {
            return false;
        }

        boolean isCxfLoggingInfoEnabled = LoggerFactory.getLogger(ORG_APACHE_CXF_CATEGORY).isInfoEnabled();
        LOG.debug("[{}] is set to INFO=[{}]", ORG_APACHE_CXF_CATEGORY, isCxfLoggingInfoEnabled);

        return isCxfLoggingInfoEnabled;
    }

    private String replaceInPayload(final String payload, final String xmlTag) {
        String newPayload = payload;
        //C2 -> C3
        if (payload.contains(xmlTag)) {
            String[] payloadSplits = payload.split(CONTENT_TYPE_MARKER);
            //keeping only first 2 Content-Type elements
            if (payloadSplits.length >= 2) {
                newPayload = payloadSplits[0] + CONTENT_TYPE_MARKER + payloadSplits[1] + AbstractLoggingInterceptor.CONTENT_SUPPRESSED;
            }
        }
        return newPayload;
    }

}
