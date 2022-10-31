package eu.domibus.core.earchive.alerts;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default alert config manager generated automatically for an alert type ( if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
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
            final Integer delay = Integer.valueOf(domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".delay_days"));
            config.setEventDelay(delay);

            final Integer frequency = Integer.valueOf(domibusPropertyProvider.getProperty(domibusPropertiesPrefix + ".frequency_days"));
            config.setEventFrequency(frequency);

            return config;
        } catch (Exception ex) {
            LOG.warn("Error while configuring alerts of type [{}] notifications for domain:[{}].",
                    alertType, domainContextProvider.getCurrentDomainSafely(), ex);
            return createAlertConfiguration(alertType);
        }
    }

    @Override
    protected RepetitiveAlertConfiguration createAlertConfiguration(AlertType alertType) {
        return new RepetitiveAlertConfiguration(alertType);
    }
}
