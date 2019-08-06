package eu.domibus.core.alerts.model.service;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Component
public class RepetitiveAlertConfigurationHolder {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(RepetitiveAlertConfigurationHolder.class);

    @Autowired
    ApplicationContext applicationContext;

    private final HashMap<AlertType, ConfigurationLoader<RepetitiveAlertModuleConfiguration>> configurations = new HashMap<>();

    public ConfigurationLoader<RepetitiveAlertModuleConfiguration> getOrCreate(AlertType alertType) {
        LOG.debug("Retrieving repetitive alert configuration for alert type :[{}]", alertType);
        if (this.configurations.get(alertType) == null) {
            synchronized (this.configurations) {
                if (this.configurations.get(alertType) == null) { //NOSONAR: double-check locking
                    LOG.debug("Creating repetitive alert configuration for alert type :[{}]", alertType);
                    ConfigurationLoader<RepetitiveAlertModuleConfiguration> bean = applicationContext.getBean(ConfigurationLoader.class);
                    this.configurations.put(alertType, bean);
                }
            }
        }
        return configurations.get(alertType);
    }

    public void resetConfiguration(AlertType alertType, Domain domain) {
        ConfigurationLoader<RepetitiveAlertModuleConfiguration> conf = configurations.get(AlertType.PASSWORD_IMMINENT_EXPIRATION);
        if(conf != null) {
            conf.resetConfiguration(domain);
        }
    }

    public void resetConfiguration(Domain domain) {
        Arrays.asList(AlertType.values()).forEach(alertType -> {
            if (configurations.containsKey(alertType)) {
                try {
                    configurations.get(alertType).resetConfiguration(domain);
                } catch (Exception ex) {
                    LOG.debug("Error reseting repetitive alert configuration for alert type :[{}]", alertType);
                }
            }
        });
    }
}



