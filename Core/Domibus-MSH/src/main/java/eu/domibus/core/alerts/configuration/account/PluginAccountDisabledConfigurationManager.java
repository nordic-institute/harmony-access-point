package eu.domibus.core.alerts.configuration.account;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default alert config manager generated automatically for an alert type ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PluginAccountDisabledConfigurationManager
        extends AccountDisabledConfigurationManager
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PluginAccountDisabledConfigurationManager.class);

    public PluginAccountDisabledConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    protected boolean checkingAuthProviderEnabled() {
        return false;
    }
}
