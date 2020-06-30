package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Endpoint;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.MTOM_ENABLED_PROPERTY;
import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.SCHEMA_VALIDATION_ENABLED_PROPERTY;

/**
 * Handles the change of wsplugin.schema.validation.enabled property of backendInterfaceEndpoint
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class SchemaValidationEnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SchemaValidationEnabledChangeListener.class);

    private Endpoint backendInterfaceEndpoint;

    public SchemaValidationEnabledChangeListener(Endpoint backendInterfaceEndpoint) {
        this.backendInterfaceEndpoint = backendInterfaceEndpoint;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, SCHEMA_VALIDATION_ENABLED_PROPERTY);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.trace("Setting [{}] property to [{}] on domain: [{}]", propertyName, propertyValue, domainCode);
        backendInterfaceEndpoint.getProperties().put("schema-validation-enabled", propertyValue);
    }
}
