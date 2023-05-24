package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.generic.DefaultAlertConfigurationChangeListener;
import eu.domibus.core.alerts.configuration.generic.DefaultConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.DefaultFrequencyAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.generic.DefaultRepetitiveAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.global.CommonConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertCategory;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_INSTANCE_NAME;

/**
 * @author Thomas Dussart,
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class AlertConfigurationServiceImpl implements AlertConfigurationService {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertConfigurationServiceImpl.class);

    public static final String DOMIBUS_ALERT_SUPER_INSTANCE_NAME_SUBJECT = DOMIBUS_INSTANCE_NAME;

    protected Map<AlertType, AlertConfigurationManager> alertConfigurationManagers = new HashMap<>();

    protected final DomibusPropertyProvider domibusPropertyProvider;

    private final ApplicationContext applicationContext;

    protected final CommonConfigurationManager commonConfigurationManager;

    public AlertConfigurationServiceImpl(DomibusPropertyProvider domibusPropertyProvider,
                                         CommonConfigurationManager commonConfigurationManager,
                                         ApplicationContext applicationContext) {
        this.domibusPropertyProvider = domibusPropertyProvider;
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
    public AlertModuleConfiguration getConfiguration(AlertType alertType) {
        AlertConfigurationManager configurationManager = getConfigurationManager(alertType);
        return configurationManager.getConfiguration();
    }

    @Override
    public AlertConfigurationManager getConfigurationManager(AlertType alertType) {
        if (!alertConfigurationManagers.containsKey(alertType)) {
            AlertConfigurationManager configurationManager = createConfigurationManager(alertType);
            applicationContext.getBean(DefaultAlertConfigurationChangeListener.class, alertType, this);
            alertConfigurationManagers.put(alertType, configurationManager);
        }
        return alertConfigurationManagers.get(alertType);
    }

    private AlertConfigurationManager createConfigurationManager(AlertType alertType) {
        String configurationProperty = alertType.getConfigurationProperty();
        Class configurationManagerClass = alertType.getConfigurationManagerClass();
        if (configurationManagerClass != null) {
            LOG.debug("Create custom class [{}] configuration manager for alert [{}] ", configurationManagerClass, alertType);
            return (AlertConfigurationManager) applicationContext.getBean(configurationManagerClass, alertType);
        }
        if (StringUtils.isNotBlank(configurationProperty)) {
            AlertCategory alertCategory = alertType.getCategory();
            if (alertCategory == null) {
                LOG.info("Could not create a configuration manager for alert [{}] because the category is not specified.", alertType);
                return null;
            }
            if (alertCategory == AlertCategory.DEFAULT) {
                LOG.debug("Create default configuration manager for alert [{}] ", alertType);
                return applicationContext.getBean(DefaultConfigurationManager.class, alertType);
            }
            if (alertCategory == AlertCategory.REPETITIVE) {
                LOG.debug("Create repetitive configuration manager for alert [{}] ", alertType);
                return applicationContext.getBean(DefaultRepetitiveAlertConfigurationManager.class, alertType);
            }
            if (alertCategory == AlertCategory.WITH_FREQUENCY) {
                LOG.debug("Create frequency configuration manager for alert [{}] ", alertType);
                return applicationContext.getBean(DefaultFrequencyAlertConfigurationManager.class, alertType);
            }
        }
        throw new ConfigurationException(String.format("Could not create a configuration manager for alert [%s]: no configurationManagerClass or configurationProperty is specified.", alertType));
    }

}
