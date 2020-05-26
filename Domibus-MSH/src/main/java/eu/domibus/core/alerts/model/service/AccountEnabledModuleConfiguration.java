package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author François Gautier
 * @since 4.2.0
 */
public class AccountEnabledModuleConfiguration extends AlertModuleConfigurationBase {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AccountEnabledModuleConfiguration.class);


    public AccountEnabledModuleConfiguration(AlertType alertType) {
        super(alertType);
    }

    public AccountEnabledModuleConfiguration(AlertType alertType, AlertLevel alertLevel, AccountDisabledMoment moment, String mailSubject) {
        super(alertType, alertLevel, mailSubject);
    }

}
