package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.webService.configuration.WSPluginConfiguration.*;

/**
 * Handles the change of
 * domibus.logging.cxf.limit
 * domibus.logging.payload.print
 * domibus.logging.metadata.print
 * property of backendInterfaceEndpoint
 *
 * @author François Gautier
 * @author Catalin Enache
 * @since 4.2
 */
@Service
public class WSPluginLoggingApacheCXFChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingApacheCXFChangeListener.class);

    private final LoggingFeature wsLoggingFeature;
    private final WSPluginLoggingEventSender wsPluginLoggingEventSender;

    public WSPluginLoggingApacheCXFChangeListener(@Qualifier("wsLoggingFeature") LoggingFeature wsLoggingFeature,
                                                  @Qualifier("wsPluginLoggingEventSender") WSPluginLoggingEventSender wsPluginLoggingEventSender) {
        this.wsLoggingFeature = wsLoggingFeature;
        this.wsPluginLoggingEventSender = wsPluginLoggingEventSender;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_LOGGING_CXF_LIMIT,
                DOMIBUS_LOGGING_PAYLOAD_PRINT,
                DOMIBUS_LOGGING_METADATA_PRINT);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        String logDebugMsg = "Setting [{}] property to [{}] on domain: [{}]";
        switch (propertyName) {
            case DOMIBUS_LOGGING_CXF_LIMIT:
                int cxfLimit = NumberUtils.toInt(propertyValue);
                LOG.debug(logDebugMsg, propertyName, cxfLimit, domainCode);
                wsLoggingFeature.setLimit(cxfLimit);
                break;
            case DOMIBUS_LOGGING_PAYLOAD_PRINT:
                boolean printPayload = BooleanUtils.toBoolean(propertyValue);
                LOG.debug(logDebugMsg, propertyName, printPayload, domainCode);
                wsPluginLoggingEventSender.setPrintPayload(printPayload);
                break;
            case DOMIBUS_LOGGING_METADATA_PRINT:
                boolean metadataPrint = BooleanUtils.toBoolean(propertyValue);
                LOG.debug(logDebugMsg, propertyName, metadataPrint, domainCode);
                wsPluginLoggingEventSender.setPrintMetadata(metadataPrint);
                break;
        }
    }
}
