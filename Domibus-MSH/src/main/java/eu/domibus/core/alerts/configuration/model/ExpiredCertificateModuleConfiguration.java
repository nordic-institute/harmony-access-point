package eu.domibus.core.alerts.configuration.model;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

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

