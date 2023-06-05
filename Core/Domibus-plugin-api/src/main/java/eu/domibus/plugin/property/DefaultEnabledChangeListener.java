package eu.domibus.plugin.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Base property change listener class for enabling/disabling a plugin on a domain
 */
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

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.debug("Executing enabled listener of plugin [{}] on domain [{}] for property [{}] with value [{}]", getPluginName(), domainCode, propertyName, propertyValue);
        boolean enable = BooleanUtils.toBoolean(propertyValue);
        doSetEnabled(domainCode, enable);
    }

    protected void doSetEnabled(final String domainCode, final boolean enabled) {
        String pluginName = getPluginName();
        LOG.debug("Setting plugin [{}] to [{}] for domain [{}].", pluginName, enabled ? "enabled" : "disabled", domainCode);
        if (enabled) {
            backendConnectorProviderExtService.backendConnectorEnabled(pluginName, domainCode);
        } else {
            backendConnectorProviderExtService.backendConnectorDisabled(pluginName, domainCode);
        }
    }

    /**
     * The name of the property used for enabling and disabling a plugin on a domain
     * @return
     */
    protected abstract String getEnabledPropertyName();

    /**
     * The name of the plugin
     * @return
     */
    protected abstract String getPluginName();
}
