package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.global.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertCategory;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.generic.DefaultAlertConfigurationChangeListener;
import eu.domibus.core.alerts.configuration.generic.DefaultConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.DefaultFrequencyAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.DefaultRepetitiveAlertConfigurationManager;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    public static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    private final ApplicationContext applicationContext;

//    protected List<AlertConfigurationManager> alertConfigurationManagers;

    Map<AlertType, AlertConfigurationManager> alertConfigurationManagers = new HashMap<>();

    protected final CommonConfigurationManager commonConfigurationManager;

    public AlertConfigurationServiceImpl(DomibusPropertyProvider domibusPropertyProvider,
//                                         @Lazy List<AlertConfigurationManager> alertConfigurationManagers,
                                         @Lazy CommonConfigurationManager commonConfigurationManager,
                                         ApplicationContext applicationContext) {
        this.domibusPropertyProvider = domibusPropertyProvider;
//        this.alertConfigurationManagers = alertConfigurationManagers;
        this.commonConfigurationManager = commonConfigurationManager;
        this.applicationContext = applicationContext;
    }

    @Override
    public void resetAll() {
        LOG.debug("Resetting all alert configurations.");
        commonConfigurationManager.reset();
        Arrays.stream(AlertType.values())
                .map(this::getConfigurationManager)
                .filter(Objects::nonNull)
                .forEach(AlertConfigurationManager::reset);
    }

    @Override
    public String getMailSubject(AlertType alertType) {
        return getConfiguration(alertType).getMailSubject();
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
    public AlertModuleConfiguration getConfiguration(AlertType alertType) {
        return getConfigurationManager(alertType).getConfiguration();
//        return getModuleConfigurationManager(alertType).getConfiguration();
    }

    @Override
    public AlertConfigurationManager getConfigurationManager(AlertType alertType) {
        if (!alertConfigurationManagers.containsKey(alertType)) {
            AlertConfigurationManager configurationManager = createConfigurationManager(alertType);
            DefaultAlertConfigurationChangeListener propertyChangeListener = applicationContext.getBean(DefaultAlertConfigurationChangeListener.class, alertType);
            alertConfigurationManagers.put(alertType, configurationManager);
        }
        return alertConfigurationManagers.get(alertType);
    }

    private AlertConfigurationManager createConfigurationManager(AlertType alertType) {
        AlertConfigurationManager alertConfigurationManager = null;
        String configurationProperty = alertType.getConfigurationProperty();
        Class configurationManagerClass = alertType.getConfigurationManagerClass();
        if (configurationManagerClass != null) {
            alertConfigurationManager = (AlertConfigurationManager) applicationContext.getBean(configurationManagerClass, alertType);
        } else if (StringUtils.isNotBlank(configurationProperty)) {
            AlertCategory alertCategory = alertType.getCategory();
            if (alertCategory == AlertCategory.DEFAULT) {
                alertConfigurationManager = applicationContext.getBean(DefaultConfigurationManager.class, alertType);
            } else if (alertCategory == AlertCategory.REPETITIVE) {
                alertConfigurationManager = applicationContext.getBean(DefaultRepetitiveAlertConfigurationManager.class, alertType);
            } else {
                alertConfigurationManager = applicationContext.getBean(DefaultFrequencyAlertConfigurationManager.class, alertType);
            }
        }
        LOG.debug("Configuration manager [{}] created for alert type [{}]", alertConfigurationManager, alertType);
        return alertConfigurationManager;
    }

//    protected AlertConfigurationManager getModuleConfigurationManager(AlertType alertType) {
        // an alert type is allowed to lack a AlertConfigurationManager
//        return alertConfigurationManagers.get(alertType);
//        return alertConfigurationManagers.stream()
//                .filter(el -> el.getAlertType() == alertType)
//                .findFirst()
//                .orElse(null);
//    }
}
