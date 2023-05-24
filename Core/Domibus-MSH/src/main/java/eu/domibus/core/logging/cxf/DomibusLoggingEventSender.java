package eu.domibus.core.logging.cxf;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
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
public class DomibusLoggingEventSender extends Slf4jEventSender  {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingEventSender.class);
    private static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";

    private boolean printPayload;

    private boolean printMetadata;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    public void setPrintMetadata(boolean printMetadata) {
        this.printMetadata = printMetadata;
    }

    @Autowired
    DomibusLoggingEventHelper domibusLoggingEventHelper;

    @Override
    protected String getLogMessage(LogEvent event) {
        if (!isCxfLoggingInfoEnabled()) {
            return StringUtils.EMPTY;
        }
        if (checkIfStripPayloadPossible()) {
            try {
                domibusLoggingEventHelper.stripPayload(event);
            } catch (RuntimeException e) {
                LOG.error("Exception while stripping the payload: ", e);
            }
        }
        if (printMetadata) {
            LOG.debug("Apache CXF logging metadata will be printed");
            return LogMessageFormatter.format(event);
        }
        return event.getPayload();
    }

    protected boolean checkIfStripPayloadPossible() {
        LOG.debug("Printing payload is{}active", printPayload ? " " : " not ");
        return !printPayload;
    }

    protected boolean isCxfLoggingInfoEnabled() {
        boolean isCxfLoggingInfoEnabled = LoggerFactory.getLogger(ORG_APACHE_CXF_CATEGORY).isInfoEnabled();
        LOG.debug("[{}] is {}set to INFO level", ORG_APACHE_CXF_CATEGORY, isCxfLoggingInfoEnabled ? StringUtils.EMPTY : "not ");
        return isCxfLoggingInfoEnabled;
    }

}
