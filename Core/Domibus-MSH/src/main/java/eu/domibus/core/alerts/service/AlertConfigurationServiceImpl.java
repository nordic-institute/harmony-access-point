package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart,
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class AlertConfigurationServiceImpl implements AlertConfigurationService {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertConfigurationServiceImpl.class);

    static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    protected final DomibusPropertyProvider domibusPropertyProvider;

//    protected List<AlertConfigurationManager> alertConfigurationManagers;

    protected final CommonConfigurationManager commonConfigurationManager;

    public AlertConfigurationServiceImpl(DomibusPropertyProvider domibusPropertyProvider,
//                                         @Lazy List<AlertConfigurationManager> alertConfigurationManagers,
                                         @Lazy CommonConfigurationManager commonConfigurationManager,
                                         ApplicationContext applicationContext) {
        this.domibusPropertyProvider = domibusPropertyProvider;
//        this.alertConfigurationManagers = alertConfigurationManagers;
        this.commonConfigurationManager = commonConfigurationManager;

        AlertType.setApplicationContext(applicationContext);
        // or maybe call setConfManager for all alert types here
    }

    @Override
    public void resetAll() {
        LOG.debug("Resetting all alert configurations.");
        commonConfigurationManager.reset();
        Arrays.stream(AlertType.values())
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
        return alertType.getConfigurationManager();
//        return alertConfigurationManagers.stream()
//                .filter(el -> el.getAlertType() == alertType)
//                .findFirst()
//                .orElse(null);
    }
}
