package eu.domibus.core.alerts.configuration.account.disabled;

import eu.domibus.core.alerts.configuration.common.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AccountDisabledModuleConfiguration extends AlertModuleConfigurationBase {

    private AccountDisabledMoment moment;

    public AccountDisabledModuleConfiguration(AlertType alertType) {
        super(alertType);
    }

//    public AccountDisabledModuleConfiguration(AlertType alertType, AlertLevel alertLevel, AccountDisabledMoment moment, String mailSubject) {
//        super(alertType, alertLevel, mailSubject);
//        this.accountDisabledMoment = moment;
//    }

    public Boolean shouldTriggerAccountDisabledAtEachLogin() {
        return isActive() && moment == AccountDisabledMoment.AT_LOGON;
    }

    public void setMoment(AccountDisabledMoment moment) {
        this.moment = moment;
    }
}
