package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.model.UserAuthenticationConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.model.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

/**
 * Base code for reading of console and plugin user account disabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public abstract class AccountDisabledConfigurationReader implements UserAuthenticationConfiguration {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(AccountDisabledConfigurationReader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    protected abstract AlertType getAlertType();

    protected abstract String getModuleName();

    protected abstract String getAlertActivePropertyName();

    protected abstract String getAlertLevelPropertyName();

    protected abstract String getAlertMomentPropertyName();

    protected abstract String getAlertEmailSubjectPropertyName();

    public AccountDisabledModuleConfiguration readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            if (shouldCheckExtAuthEnabled()) {
                //ECAS or other provider
                LOG.debug("domain:[{}] [{}] module is inactive for the following reason: external authentication provider is enabled", currentDomain, getModuleName());
                return new AccountDisabledModuleConfiguration(getAlertType());
            }

            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean accountDisabledActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());
            if (!alertActive || !accountDisabledActive) {
                LOG.debug("domain:[{}] [{}] module is inactive for the following reason: global alert module active:[{}], account disabled module active:[{}]"
                        , currentDomain, getModuleName(), alertActive, accountDisabledActive);
                return new AccountDisabledModuleConfiguration(getAlertType());
            }

            final AlertLevel level = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));
            final AccountDisabledMoment moment = AccountDisabledMoment.valueOf(domibusPropertyProvider.getProperty(getAlertMomentPropertyName()));
            final String mailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

            LOG.info("[{}] module activated for domain:[{}]", getModuleName(), currentDomain);
            return new AccountDisabledModuleConfiguration(getAlertType(), level, moment, mailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading [{}] module configuration for domain:[{}], ", getModuleName(), currentDomain, e);
            return new AccountDisabledModuleConfiguration(getAlertType());
        }
    }

    private Boolean isAlertModuleEnabled() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
    }
}
