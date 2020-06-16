package eu.domibus.core.alerts.configuration.certificate.imminent;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ImminentExpirationCertificateModuleConfiguration extends AlertModuleConfigurationBase {

    private Integer imminentExpirationDelay;
    private Integer imminentExpirationFrequency;

    public ImminentExpirationCertificateModuleConfiguration() {
        super(AlertType.CERT_IMMINENT_EXPIRATION);
    }

    public ImminentExpirationCertificateModuleConfiguration(
            Integer imminentExpirationDelay,
            Integer imminentExpirationFrequency,
            AlertLevel imminentExpirationAlertLevel,
            String imminentExpirationMailSubject) {

        super(AlertType.CERT_IMMINENT_EXPIRATION, imminentExpirationAlertLevel, imminentExpirationMailSubject);

        this.imminentExpirationDelay = imminentExpirationDelay;
        this.imminentExpirationFrequency = imminentExpirationFrequency;
    }

    public Integer getImminentExpirationDelay() {
        return imminentExpirationDelay;
    }

    public Integer getImminentExpirationFrequency() {
        return imminentExpirationFrequency;
    }

}
