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
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FrequencyAlertConfigurationManager
        extends BaseConfigurationManager<FrequencyAlertConfiguration>
        implements AlertConfigurationManager {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(FrequencyAlertConfigurationManager.class);

    public FrequencyAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

    public FrequencyAlertConfiguration readConfiguration() {
        FrequencyAlertConfiguration config = super.readConfiguration();
        if (!config.isActive()) {
            return config;
        }

        try {
            final Integer frequency = domibusPropertyProvider.getIntegerProperty(getFrequencyPropertyName());
            config.setFrequency(frequency);

            return config;
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts of type [{}] notifications for domain:[{}].",
                    alertType, domainContextProvider.getCurrentDomainSafely(), ex);
            return createAlertConfiguration(alertType);
        }
    }

    protected String getFrequencyPropertyName() {
        return domibusPropertiesPrefix + ".frequency_days";
    }

    @Override
    protected FrequencyAlertConfiguration createAlertConfiguration(AlertType alertType) {
        return new FrequencyAlertConfiguration(alertType);
    }
}
