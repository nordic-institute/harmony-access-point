package eu.domibus.logging;

import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";

    private boolean printPayload;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Autowired
    DomibusLoggingEventHelper domibusLoggingEventHelper;

    @Override
    protected String getLogMessage(LogEvent event) {
        if (checkIfStripPayloadPossible()) {
            try {
                domibusLoggingEventHelper.stripPayload(event);
            } catch (RuntimeException e) {
                LOG.error("Exception while stripping the payload: ", e);
            }
        }
        return LogMessageFormatter.format(event);
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
}
