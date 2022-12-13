package eu.domibus.core.alerts.configuration.account;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.common.BaseConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom alert config manager for account disabled alerts
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

    public AccountDisabledConfigurationManager(AlertType alertType) {
        super(alertType);
    }

    @Override
    public AccountDisabledModuleConfiguration readConfiguration() {
        if (checkingAuthProviderEnabled()) {
            LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", alertType.getTitle());
            return createNewInstance(alertType);
        }

        AccountDisabledModuleConfiguration conf = super.readConfiguration();
        if (!conf.isActive()) {
            return conf;
        }

        final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".moment"));
        conf.setMoment(moment);

        return conf;
    }

    protected abstract boolean checkingAuthProviderEnabled();

    @Override
    protected AccountDisabledModuleConfiguration createNewInstance(AlertType alertType) {
        return new AccountDisabledModuleConfiguration(alertType);
    }

    @Override
    protected String getMailSubject() {
        return domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".subject");
    }
}
