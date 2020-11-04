package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.webService.configuration.WSPluginConfiguration.DOMIBUS_LOGGING_METADATA_PRINT;

/**
 * Handles the change of domibus.logging.metadata.print property of backendInterfaceEndpoint
 *
 * @author François Gautier
 * @since 4.2
 */
@Service("wsPluginLoggingMetadataPrintChangeListener")
public class WSPluginLoggingMetadataPrintChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginLoggingMetadataPrintChangeListener.class);

    private final WSPluginLoggingEventSender wsPluginLoggingEventSender;

    public WSPluginLoggingMetadataPrintChangeListener(@Qualifier("wsPluginLoggingEventSender") WSPluginLoggingEventSender  wsPluginLoggingEventSender) {
        this.wsPluginLoggingEventSender = wsPluginLoggingEventSender;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, DOMIBUS_LOGGING_METADATA_PRINT);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        boolean val = BooleanUtils.isTrue(Boolean.valueOf(propertyValue));
        LOG.trace("Setting [{}] property to [{}] on domain: [{}] for wsPluginLoggingEventSender", propertyName, val, domainCode);
        wsPluginLoggingEventSender.setPrintMetadata(val);
    }
}
