package eu.domibus.core.alerts.configuration.generic;

import eu.domibus.core.alerts.configuration.common.AlertConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default alert config manager generated automatically for an alert type (if not overridden)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DefaultFrequencyAlertConfigurationManager extends FrequencyAlertConfigurationManager
        implements AlertConfigurationManager {

    public DefaultFrequencyAlertConfigurationManager(AlertType alertType) {
        super(alertType);
    }

}
