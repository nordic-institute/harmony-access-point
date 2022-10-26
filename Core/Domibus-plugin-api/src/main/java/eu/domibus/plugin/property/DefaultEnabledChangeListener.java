package eu.domibus.plugin.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Base class for enabling/disabling property change listener
 */
@Component
public abstract class DefaultEnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultEnabledChangeListener.class);

    final protected BackendConnectorProviderExtService backendConnectorProviderExtService;

    public DefaultEnabledChangeListener(BackendConnectorProviderExtService backendConnectorProviderExtService) {
        this.backendConnectorProviderExtService = backendConnectorProviderExtService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equals(getEnabledPropertyName(), propertyName);
    }

    protected abstract CharSequence getEnabledPropertyName();

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.debug("Executing enabled listener on domain [{}] for property [{}] with value [{}]", domainCode, propertyName, propertyValue);
        boolean enable = BooleanUtils.toBoolean(propertyValue);
        doSetEnabled(domainCode, enable);
    }

    public void doSetEnabled(final String domainCode, final boolean enabled) {
        String pluginName = getName();
        LOG.info("Setting plugin [{}] to [{}] for domain [{}].", pluginName, enabled ? "enabled" : "disabled", domainCode);
        if (enabled) {
            backendConnectorProviderExtService.backendConnectorEnabled(pluginName, domainCode);
        } else {
            backendConnectorProviderExtService.backendConnectorDisabled(pluginName, domainCode);
        }
    }

    protected abstract String getName();
}
