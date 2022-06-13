package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
@Service
public class AlertConfigurationServiceImpl implements AlertConfigurationService {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertConfigurationServiceImpl.class);

    static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected List<AlertConfigurationManager> alertConfigurationManagers;

    @Autowired
    protected CommonConfigurationManager commonConfigurationManager;

    @Override
    public void resetAll() {
        LOG.debug("Resetting all configurations.");
        commonConfigurationManager.reset();
        Arrays.asList(AlertType.values()).stream()
                .map(this::getModuleConfigurationManager)
                .filter(Objects::nonNull)
                .forEach(AlertConfigurationManager::reset);
    }

    @Override
    public String getMailSubject(AlertType alertType) {
        return getModuleConfiguration(alertType).getMailSubject();
    }

    @Override
    public Boolean isAlertModuleEnabled() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
    }

    @Override
    public Boolean isSendEmailActive() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_MAIL_SENDING_ACTIVE);
    }

    @Override
    public AlertModuleConfiguration getModuleConfiguration(AlertType alertType) {
        return getModuleConfigurationManager(alertType).getConfiguration();
    }

    protected AlertConfigurationManager getModuleConfigurationManager(AlertType alertType) {
        // an alert type is allowed to lack a AlertConfigurationManager
        return alertConfigurationManagers.stream()
                .filter(el -> el.getAlertType() == alertType)
                .findFirst()
                .orElse(null);
    }
}
