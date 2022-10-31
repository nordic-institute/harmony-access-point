//package eu.domibus.core.alerts.configuration.certificate.imminent;
//
//import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
//import eu.domibus.api.alerts.AlertLevel;
//import eu.domibus.core.alerts.model.common.AlertType;
//
///**
// * @author Thomas Dussart
// * @since 4.0
// */
//public class ImminentExpirationCertificateModuleConfiguration extends AlertModuleConfigurationBase {
//
//    private Integer eventDelay;
//    private Integer eventFrequency;
//
//    public ImminentExpirationCertificateModuleConfiguration() {
//        super(AlertType.CERT_IMMINENT_EXPIRATION);
//    }
//
//    public ImminentExpirationCertificateModuleConfiguration(
//            Integer eventDelay,
//            Integer eventFrequency,
//            AlertLevel imminentExpirationAlertLevel,
//            String imminentExpirationMailSubject) {
//
//        super(AlertType.CERT_IMMINENT_EXPIRATION, imminentExpirationAlertLevel, imminentExpirationMailSubject);
//
//        this.eventDelay = eventDelay;
//        this.eventFrequency = eventFrequency;
//    }
//
//    public Integer getEventDelay() {
//        return eventDelay;
//    }
//
//    public Integer getEventFrequency() {
//        return eventFrequency;
//    }
//
//}
