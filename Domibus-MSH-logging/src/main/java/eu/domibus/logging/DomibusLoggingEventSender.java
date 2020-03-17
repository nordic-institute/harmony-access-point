package eu.domibus.logging;

import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;

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
    private static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";
    public static final String HEADERS_AUTHORIZATION = "Authorization";

    private boolean printPayload;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Override
    protected String getLogMessage(LogEvent event) {
        LOG.debug("printPayload=[{}]", printPayload);

        boolean isCxfLoggingInfoEnabled = LoggerFactory.getLogger(ORG_APACHE_CXF_CATEGORY).isInfoEnabled();
        LOG.debug("[{}] set to INFO=[{}]", ORG_APACHE_CXF_CATEGORY, isCxfLoggingInfoEnabled);

        try {
            if (isCxfLoggingInfoEnabled ) {
                stripHeaders(event);
                if (!printPayload) {
                    stripPayload(event);
                }

            }
        } catch (RuntimeException e) {
            LOG.error("Exception while stripping the payload: ", e);
        }

        return LogMessageFormatter.format(event);
    }

    /**
     * It removes some parts of the payload info
     *
     * @param event
     */
    protected void stripPayload(LogEvent event) {
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
        }
    }

    protected void stripHeaders(LogEvent event) {
        Map<String, String> headers = event.getHeaders();
        if (CollectionUtils.isEmpty(headers)) {
            LOG.debug("no apache cxf headers to strip");
            return;
        }
        headers.entrySet()
                .removeIf(e -> HEADERS_AUTHORIZATION.equalsIgnoreCase(e.getKey()));
    }

}
