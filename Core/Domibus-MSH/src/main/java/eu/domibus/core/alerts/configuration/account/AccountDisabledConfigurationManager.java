package eu.domibus.core.alerts.configuration.account;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.BaseConfigurationManager;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;
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
public abstract class AccountDisabledConfigurationManager
        extends BaseConfigurationManager<AccountDisabledModuleConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AccountDisabledConfigurationManager.class);

    public AccountDisabledConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    @Override
    public AccountDisabledModuleConfiguration readConfiguration() {
        if (checkingAuthProviderEnabled()) {
            LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", alertType.getTitle());
            return createAlertConfiguration(alertType);
        }

        AccountDisabledModuleConfiguration conf = super.readConfiguration();

        final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".moment"));
        conf.setMoment(moment);

        return conf;
    }

    protected abstract boolean checkingAuthProviderEnabled();

    @Override
    protected AccountDisabledModuleConfiguration createAlertConfiguration(AlertType alertType) {
        return new AccountDisabledModuleConfiguration(alertType);
    }
}
