package eu.domibus.plugin.webService.logging;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    public void setPrintPayload(boolean printPayload) {
        this.printPayload = printPayload;
    }

    @Autowired
    WSPluginLoggingEventHelper wsPluginLoggingEventHelper;

    @Override
    protected String getLogMessage(LogEvent event) {
        try {
            if (checkIfApacheCxfLoggingInfoEnabled()) {
                wsPluginLoggingEventHelper.stripHeaders(event);
                if (checkIfStripPayloadPossible()) {
                    wsPluginLoggingEventHelper.stripPayload(event);
                }
            }
        } catch (RuntimeException e) {
            LOG.error("Exception while stripping the payload: ", e);
        }
        return LogMessageFormatter.format(event);
    }

    protected boolean checkIfStripPayloadPossible() {
        LOG.debug("Printing payload is{}active", printPayload ? " " : " not ");
        return !printPayload;
    }

    protected boolean checkIfApacheCxfLoggingInfoEnabled() {
        boolean isCxfLoggingInfoEnabled = LoggerFactory.getLogger(ORG_APACHE_CXF_CATEGORY).isInfoEnabled();
        if (isCxfLoggingInfoEnabled) {
            LOG.debug("[{}] is set to at least INFO level", ORG_APACHE_CXF_CATEGORY);
        }
        return isCxfLoggingInfoEnabled;
    }

}
