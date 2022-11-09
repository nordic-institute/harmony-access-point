package eu.domibus.core.alerts.configuration.account;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom alert config manager for plugin account disabled alerts
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PluginAccountDisabledConfigurationManager
        extends AccountDisabledConfigurationManager
        implements AlertConfigurationManager {

    public PluginAccountDisabledConfigurationManager(AlertType alertType) {
        super(alertType);
    }

    @Override
    protected boolean checkingAuthProviderEnabled() {
        return false;
    }
}
