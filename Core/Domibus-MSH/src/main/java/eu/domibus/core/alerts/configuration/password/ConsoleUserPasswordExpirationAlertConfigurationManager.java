package eu.domibus.core.alerts.configuration.password;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfigurationManager;
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
public class ConsoleUserPasswordExpirationAlertConfigurationManager extends RepetitiveAlertConfigurationManager
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConsoleUserPasswordExpirationAlertConfigurationManager.class);

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    public ConsoleUserPasswordExpirationAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    public RepetitiveAlertConfiguration readConfiguration() {
        if (domibusConfigurationService.isExtAuthProviderEnabled()) {
            LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", getAlertType().getTitle());
            return createAlertConfiguration(getAlertType());
        }

        return super.readConfiguration();
    }

}
