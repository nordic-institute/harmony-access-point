package eu.domibus.plugin.ws.property.listeners;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of ws-plugin for the current domain.
 */
@Component
public class EnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EnabledChangeListener.class);

    final protected WSPluginImpl wsPlugin;

    public EnabledChangeListener(WSPluginImpl wsPlugin) {
        this.wsPlugin = wsPlugin;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equals(DOMAIN_ENABLED, propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.debug("Executing enabled listener on domain [{}] for property [{}] with value [{}]", domainCode, propertyName, propertyValue);
        boolean enable = BooleanUtils.toBoolean(propertyValue);
        wsPlugin.doSetEnabled(domainCode, enable);
    }

}
