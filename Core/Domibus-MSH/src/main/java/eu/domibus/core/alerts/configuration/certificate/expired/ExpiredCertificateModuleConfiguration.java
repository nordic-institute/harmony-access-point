//package eu.domibus.core.alerts.configuration.certificate.expired;
//
//import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
//import eu.domibus.api.alerts.AlertLevel;
//import eu.domibus.core.alerts.model.common.AlertType;
//
///**
// * @author Thomas Dussart
// * @since 4.0
// */
//public class ExpiredCertificateModuleConfiguration extends AlertModuleConfigurationBase {
//
//    private Integer eventFrequency;
//    private Integer eventDuration;
//
//    public ExpiredCertificateModuleConfiguration() {
//        super(AlertType.CERT_EXPIRED);
//    }
//
//    public ExpiredCertificateModuleConfiguration(Integer eventFrequency, Integer eventDuration, AlertLevel expiredLevel, String expiredMailSubject) {
//        super(AlertType.CERT_EXPIRED, expiredLevel, expiredMailSubject);
//
//        this.eventFrequency = eventFrequency;
//        this.eventDuration = eventDuration;
//    }
//
//    public Integer getEventFrequency() {
//        return eventFrequency;
//    }
//
//    public Integer getEventDuration() {
//        return eventDuration;
//    }
//
//}
//
