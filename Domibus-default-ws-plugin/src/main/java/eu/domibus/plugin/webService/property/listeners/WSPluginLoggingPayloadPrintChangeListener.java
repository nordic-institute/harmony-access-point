package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.webService.configuration.WSPluginConfiguration.DOMIBUS_LOGGING_PAYLOAD_PRINT;

/**
 * Handles the change of {@link eu.domibus.plugin.webService.configuration.WSPluginConfiguration#DOMIBUS_LOGGING_PAYLOAD_PRINT}
 * property of backendInterfaceEndpoint
 *
 * @author François Gautier
 * @since 5.0
 */
@Service("wsPluginLoggingPayloadPrintChangeListener")
public class WSPluginLoggingPayloadPrintChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingPayloadPrintChangeListener.class);

    private final WSPluginLoggingEventSender wsPluginLoggingEventSender;

    public WSPluginLoggingPayloadPrintChangeListener(@Qualifier("wsPluginLoggingEventSender") WSPluginLoggingEventSender  wsPluginLoggingEventSender) {
        this.wsPluginLoggingEventSender = wsPluginLoggingEventSender;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, DOMIBUS_LOGGING_PAYLOAD_PRINT);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        boolean val = BooleanUtils.isTrue(Boolean.valueOf(propertyValue));
        LOG.trace("Setting [{}] property to [{}] on domain: [{}] for wsPluginLoggingEventSender", propertyName, val, domainCode);
        wsPluginLoggingEventSender.setPrintPayload(val);
    }
}
