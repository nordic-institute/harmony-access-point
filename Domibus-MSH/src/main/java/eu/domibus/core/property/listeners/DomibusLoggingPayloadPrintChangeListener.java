package eu.domibus.core.property.listeners;

import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_PAYLOAD_PRINT;

/**
 * Handles the change of {@link eu.domibus.api.property.DomibusPropertyMetadataManagerSPI#DOMIBUS_LOGGING_PAYLOAD_PRINT}
 * of {@link DomibusLoggingEventSender}
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusLoggingPayloadPrintChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLoggingPayloadPrintChangeListener.class);

    private final DomibusLoggingEventSender domibusLoggingEventSender;

    public DomibusLoggingPayloadPrintChangeListener(@Qualifier("loggingSender") DomibusLoggingEventSender domibusLoggingEventSender) {
        this.domibusLoggingEventSender = domibusLoggingEventSender;
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
        LOG.trace("Setting [{}] property to [{}] on domain: [{}] for loggingSender", propertyName, val, domainCode);
        domibusLoggingEventSender.setPrintPayload(val);
    }
}
