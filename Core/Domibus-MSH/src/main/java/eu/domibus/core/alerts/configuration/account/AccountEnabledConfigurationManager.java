package eu.domibus.core.alerts.configuration.account;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.common.BaseConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.DefaultConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom alert config manager for account enabled alerts
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AccountEnabledConfigurationManager
        extends DefaultConfigurationManager
        implements AlertConfigurationManager {

    public AccountEnabledConfigurationManager(AlertType alertType) {
        super(alertType);
    }

    @Override
    protected String getMailSubject() {
        return domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".subject");
    }
}
