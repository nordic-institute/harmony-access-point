package eu.domibus.core.alerts.configuration.certificate;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ExpiredCertificateModuleConfiguration extends AlertModuleConfigurationBase {

    private Integer expiredFrequency;
    private Integer expiredDuration;

    public ExpiredCertificateModuleConfiguration() {
        super(AlertType.CERT_EXPIRED);
    }

    public ExpiredCertificateModuleConfiguration(Integer expiredFrequency, Integer expiredDuration, AlertLevel expiredLevel, String expiredMailSubject) {
        super(AlertType.CERT_EXPIRED, expiredLevel, expiredMailSubject);

        this.expiredFrequency = expiredFrequency;
        this.expiredDuration = expiredDuration;
    }

    public Integer getExpiredFrequency() {
        return expiredFrequency;
    }

    public Integer getExpiredDuration() {
        return expiredDuration;
    }

}

