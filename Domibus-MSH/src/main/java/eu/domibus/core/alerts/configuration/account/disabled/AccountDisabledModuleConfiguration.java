package eu.domibus.core.alerts.configuration.account.disabled;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AccountDisabledModuleConfiguration extends AlertModuleConfigurationBase {

    private AccountDisabledMoment accountDisabledMoment;

    public AccountDisabledModuleConfiguration(AlertType alertType) {
        super(alertType);
    }

    public AccountDisabledModuleConfiguration(AlertType alertType, AlertLevel alertLevel, AccountDisabledMoment moment, String mailSubject) {
        super(alertType, alertLevel, mailSubject);
        this.accountDisabledMoment = moment;
    }

    public Boolean shouldTriggerAccountDisabledAtEachLogin() {
        return isActive() && accountDisabledMoment == AccountDisabledMoment.AT_LOGON;
    }

}
