package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.RepetitiveAlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Custom manager for certificate expired alert because the delay property name is different
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DefaultRepetitiveAlertConfigurationManager extends RepetitiveAlertConfigurationManager
        implements AlertConfigurationManager {

    public DefaultRepetitiveAlertConfigurationManager(AlertType alertType, String domibusPropertiesPrefix) {
        super(alertType, domibusPropertiesPrefix);
    }

}
