package eu.domibus.logging;

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


    private static final String CONTENT_TYPE = "Content-Type:";

    private boolean printPayload;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Override
    protected String getLogMessage(LogEvent event) {
        LOG.debug("printPayload={}", printPayload);

        //if it's multipart and we want to strip the AS4 payload
        if (event.isMultipartContent() && !printPayload) {
            final String payload = event.getPayload();
            String[] payloadSplits = payload.split(CONTENT_TYPE);
            //keeping only first 2 Content-Type elements
            if (payloadSplits.length >= 2) {
                event.setPayload(payloadSplits[0] + CONTENT_TYPE + payloadSplits[1]);
            }
        }
        return LogMessageFormatter.format(event);
    }

}
