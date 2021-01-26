package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogMessageFormatter;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class extends the default {@code Slf4jEventSender} implemented by Apache CXF
 * <p>
 * It will implement operations based on {@code LogEvent} like partially printing the payload, etc
 *
 * @since 4.1.4
 * @author Catalin Enache
 */
public class WSPluginLoggingEventSender extends Slf4jEventSender {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingEventSender.class);

    static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";

    private boolean printPayload;

    private boolean printMetadata;

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    public void setPrintMetadata(boolean printMetadata) {
        this.printMetadata = printMetadata;
    }

    @Autowired
    WSPluginLoggingEventHelper wsPluginLoggingEventHelper;

    @Override
    protected String getLogMessage(LogEvent event) {
        if (!isCxfLoggingInfoEnabled()) {
            return StringUtils.EMPTY;
        }
        try {
            wsPluginLoggingEventHelper.stripHeaders(event);
            if (checkIfStripPayloadPossible()) {
                wsPluginLoggingEventHelper.stripPayload(event);
            }
        } catch (RuntimeException e) {
            LOG.error("Exception while stripping the payload: ", e);
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
