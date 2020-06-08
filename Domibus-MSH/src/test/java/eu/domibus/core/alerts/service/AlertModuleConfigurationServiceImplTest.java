//package eu.domibus.core.alerts.service;
//
//@RunWith(JMockit.class)
//public class AlertModuleConfigurationServiceImplTest {
//    @Tested
//    private AlertConfigurationService configurationService;
//
//

//

//

//    @Test
//    public void getRepetitiveAlertConfigurationTest() {
//        String property = "domibus.alert.password.expired";
//        new Expectations() {
//            {
//                domibusPropertyProvider.getBooleanProperty(DOMIBUS_ALERT_ACTIVE);
//                result = true;
//
//                domibusPropertyProvider.getProperty(property + ".active");
//                result = "true";
//
//                domibusPropertyProvider.getProperty(property + ".delay_days");
//                result = "15";
//
//                domibusPropertyProvider.getProperty(property + ".frequency_days");
//                result = "5";
//
//                domibusPropertyProvider.getProperty(property + ".level");
//                result = AlertLevel.MEDIUM.name();
//
//                domibusPropertyProvider.getProperty(property + ".mail.subject");
//                result = "my subjects";
//            }
//        };
//        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();
//
//        assertTrue(conf.isActive());
//        assertEquals(15, (long) conf.getEventDelay());
//        Alert a = new Alert() {{
//            setAlertType(AlertType.PASSWORD_EXPIRED);
//        }};
//        assertEquals(AlertLevel.MEDIUM, conf.getAlertLevel(a));
//
//    }
//
//    @Test
//    public void test_getRepetitiveAlertConfiguration_ExtAuthProviderEnabled() {
//        new Expectations() {
//            {
//                domibusConfigurationService.isExtAuthProviderEnabled();
//                result = true;
//            }
//        };
//        final PasswordExpirationAlertModuleConfiguration conf = new ConsolePasswordExpiredAlertConfigurationReader().readConfiguration();
//        assertFalse(conf.isActive());
//    }
//
//}