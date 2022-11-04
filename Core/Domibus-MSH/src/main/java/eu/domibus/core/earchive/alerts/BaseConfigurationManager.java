package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.ReaderMethodAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.service.ConfigurationReader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_ACTIVE;

/**
 * Default alert config manager generated automatically for an alert type ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BaseConfigurationManager<AC extends AlertModuleConfigurationBase>
        extends ReaderMethodAlertConfigurationManager<AC>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseConfigurationManager.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    protected AlertType alertType;

    protected String domibusPropertiesPrefix;

    public BaseConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        this.alertType = alertType;
        this.domibusPropertiesPrefix = domibusPropertiesPrefix;
    }

    @Override
    public AlertType getAlertType() {
        return alertType;
    }

    @Override
    public ConfigurationReader<AC> getReaderMethod() {
        return this::readConfiguration;
    }

    public AC readConfiguration() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        try {
            final Boolean alertsModuleActive = domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
            final Boolean thisAlertActive = isAlertActive();
            if (BooleanUtils.isNotTrue(alertsModuleActive) || BooleanUtils.isNotTrue(thisAlertActive)) {
                return createAlertConfiguration(alertType);
            }

            AC config = createAlertConfiguration(alertType);
            config.setActive(true);

            final AlertLevel alertLevel = getAlertLevel();
            config.setAlertLevel(alertLevel);

            final String mailSubject = getMailSubject();
            config.setMailSubject(mailSubject);

            return config;
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts of type [{}] notifications for domain:[{}].", alertType, currentDomain, ex);
            return createAlertConfiguration(alertType);
        }
    }

    protected String getMailSubject() {
        return domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".mail.subject");
    }

    protected AlertLevel getAlertLevel() {
        return AlertLevel.valueOf(domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".level"));
    }

    protected Boolean isAlertActive() {
        return domibusPropertyProvider.getBooleanProperty(domibusPropertiesPrefix + ".active");
    }

    protected AC createAlertConfiguration(AlertType alertType) {
        return null;
    }
}
