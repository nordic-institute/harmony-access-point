package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.model.UserAuthenticationConfiguration;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.model.LoginFailureModuleConfiguration;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

/**
 * Base code for reading of console and plugin user login fail alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public abstract class LoginFailConfigurationReader implements UserAuthenticationConfiguration {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(LoginFailConfigurationReader.class);

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    protected abstract AlertType getAlertType();

    protected abstract String getModuleName();

    protected abstract String getAlertActivePropertyName();

    protected abstract String getAlertLevelPropertyName();

    protected abstract String getAlertEmailSubjectPropertyName();

    public LoginFailureModuleConfiguration readConfiguration() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        try {
            if (shouldCheckExtAuthEnabled()) {
                //ECAS or other provider
                LOG.debug("[{}] module is inactive for the following reason: external authentication provider is enabled", getModuleName());
                return new LoginFailureModuleConfiguration(getAlertType());
            }

            final Boolean alertActive = isAlertModuleEnabled();
            final Boolean loginFailureActive = domibusPropertyProvider.getBooleanProperty(getAlertActivePropertyName());

            if (!alertActive || !loginFailureActive) {
                LOG.debug("{} module is inactive for the following reason:global alert module active[{}], login failure module active[{}]", getModuleName(), alertActive, loginFailureActive);
                return new LoginFailureModuleConfiguration(getAlertType());
            }

            final AlertLevel loginFailureAlertLevel = AlertLevel.valueOf(domibusPropertyProvider.getProperty(getAlertLevelPropertyName()));

            final String loginFailureMailSubject = domibusPropertyProvider.getProperty(getAlertEmailSubjectPropertyName());

            LOG.info("{} module activated for domain:[{}]", getModuleName(), domain);
            return new LoginFailureModuleConfiguration(getAlertType(), loginFailureAlertLevel, loginFailureMailSubject);

        } catch (Exception e) {
            LOG.warn("An error occurred while reading {} module configuration for domain:[{}], ", getModuleName(), domain, e);
            return new LoginFailureModuleConfiguration(getAlertType());
        }
    }

    private Boolean isAlertModuleEnabled() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
    }
}
