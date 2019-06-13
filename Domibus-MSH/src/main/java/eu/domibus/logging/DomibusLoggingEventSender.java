package eu.domibus.logging;

import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
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


    static final String CONTENT_TYPE = "Content-Type:";
    static final String RETRIEVE_MESSAGE_RESPONSE = "retrieveMessageResponse";
    static final String VALUE_START = "<value>";
    static final String VALUE_END = "</value>";

    private boolean printPayload;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Override
    protected String getLogMessage(LogEvent event) {
        LOG.debug("printPayload={}", printPayload);

        if (!printPayload) {
            stripPayload(event);
        }


        return LogMessageFormatter.format(event);
    }

    /**
     * It removes some parts of the payload info
     *
     * @param event
     */
    private void stripPayload(LogEvent event) {
        final String payload = event.getPayload();

        //C2 -> C3
        if (payload.contains(CONTENT_TYPE)) {
            //if it's multipart and we want to strip the AS4 payload
            if (event.isMultipartContent()) {

                String[] payloadSplits = payload.split(CONTENT_TYPE);
                //keeping only first 2 Content-Type elements
                if (payloadSplits.length >= 2) {
                    event.setPayload(payloadSplits[0] + CONTENT_TYPE + payloadSplits[1] + AbstractLoggingInterceptor.CONTENT_SUPPRESSED);
                }
            }

        } else if (payload.contains(RETRIEVE_MESSAGE_RESPONSE)) {
            //C4 - C3
            int indexStart = payload.indexOf(VALUE_START);
            int indexEnd = payload.indexOf(VALUE_END);
            event.setPayload(payload.replace(payload.substring(indexStart + 7, indexEnd), AbstractLoggingInterceptor.CONTENT_SUPPRESSED));
        }
    }

}
