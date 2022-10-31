package eu.domibus.core.earchive.alerts;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ConsoleUserPasswordExpirationAlertConfigurationManager extends RepetitiveAlertConfigurationManager
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConsoleUserPasswordExpirationAlertConfigurationManager.class);

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    public ConsoleUserPasswordExpirationAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    public RepetitiveAlertConfiguration readConfiguration() {
        if (domibusConfigurationService.isExtAuthProviderEnabled()) {
            LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", getAlertType().getTitle());
            return createAlertConfiguration(getAlertType());
        }

        return super.readConfiguration();
    }

    @Override
    protected RepetitiveAlertConfiguration createAlertConfiguration(AlertType alertType) {
        return new RepetitiveAlertConfiguration(alertType);
    }
}
