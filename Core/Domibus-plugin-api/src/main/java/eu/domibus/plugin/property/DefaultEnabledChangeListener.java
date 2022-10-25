package eu.domibus.plugin.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles enabling/disabling of fs-plugin for the current domain.
 */
@Component
public abstract class DefaultEnabledChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultEnabledChangeListener.class);

    final protected AbstractBackendConnector<?, ?> backendConnector;

    public DefaultEnabledChangeListener(AbstractBackendConnector<?, ?> backendConnector) {
        this.backendConnector = backendConnector;
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
        backendConnector.doSetEnabled(domainCode, enable);
    }

}
