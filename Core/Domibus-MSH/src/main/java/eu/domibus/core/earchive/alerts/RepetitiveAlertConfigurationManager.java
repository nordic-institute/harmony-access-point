package eu.domibus.core.earchive.alerts;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default alert config manager generated automatically for an alert type that has frequency and delay properties ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
public class RepetitiveAlertConfigurationManager
        extends BaseConfigurationManager<RepetitiveAlertConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(RepetitiveAlertConfigurationManager.class);

    public RepetitiveAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    public RepetitiveAlertConfiguration readConfiguration() {
        RepetitiveAlertConfiguration config = super.readConfiguration();
        if (!config.isActive()) {
            return config;
        }

        try {
            final Integer delay = domibusPropertyProvider.getIntegerProperty(getDelayPropertyName());
            config.setDelay(delay);

            final Integer frequency = domibusPropertyProvider.getIntegerProperty(getFrequencyPropertyName());
            config.setFrequency(frequency);

            return config;
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts of type [{}] notifications for domain:[{}].",
                    alertType, domainContextProvider.getCurrentDomainSafely(), ex);
            return createAlertConfiguration(alertType);
        }
    }

    protected String getDelayPropertyName() {
        return domibusPropertiesPrefix + ".delay_days";
    }

    protected String getFrequencyPropertyName() {
        return domibusPropertiesPrefix + ".frequency_days";
    }

    @Override
    protected RepetitiveAlertConfiguration createAlertConfiguration(AlertType alertType) {
        return new RepetitiveAlertConfiguration(alertType);
    }
}
