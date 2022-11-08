package eu.domibus.core.alerts.configuration.login;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.BaseConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom manager for console users because we need to check for external authentication
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ConsoleUserLoginFailAlertConfigurationManager extends BaseConfigurationManager<AlertModuleConfigurationBase>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConsoleUserLoginFailAlertConfigurationManager.class);

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    public ConsoleUserLoginFailAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    protected AlertModuleConfigurationBase createAlertConfiguration(AlertType alertType) {
        return new AlertModuleConfigurationBase(alertType);
    }

    @Override
    public AlertModuleConfigurationBase readConfiguration() {
        if (domibusConfigurationService.isExtAuthProviderEnabled()) {
            LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", alertType.getTitle());
            return createAlertConfiguration(alertType);
        }

        return super.readConfiguration();
    }

}
