package eu.domibus.core.alerts.configuration.reader;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.alerts.model.common.AlertType;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsolePasswordImminentExpirationAlertConfigurationReader extends RepetitiveAlertConfigurationReader {

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    protected AlertType getAlertType() {
        return AlertType.PASSWORD_IMMINENT_EXPIRATION;
    }

    @Override
    public boolean shouldCheckExtAuthEnabled() {
        return domibusConfigurationService.isExtAuthProviderEnabled();
    }
}
