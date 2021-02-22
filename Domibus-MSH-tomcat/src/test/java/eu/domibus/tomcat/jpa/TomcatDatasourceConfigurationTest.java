//package eu.domibus.tomcat.jpa;
//
//import eu.domibus.api.property.DomibusPropertyProvider;
//import eu.domibus.core.property.PrefixedProperties;
//import mockit.*;
//import mockit.integration.junit4.JMockit;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.Collections;
//
//import static eu.domibus.tomcat.jpa.TomcatDatasourceConfiguration.*;
//
//@RunWith(JMockit.class)
//public class TomcatDatasourceConfigurationTest {
//
//    @Tested
//    private TomcatDatasourceConfiguration tomcatDatasourceConfiguration;
//
//    @Injectable
//    private DomibusPropertyProvider domibusPropertyProvider;
//
//    @Mocked
//    private PrefixedProperties prefixedProperties;
//
//
//    @Test
//    public void xaProperties_switchesTheOracleUrlPropertyKeyToUppercase() {
//        new Expectations() {{
//            new PrefixedProperties(domibusPropertyProvider, DOMIBUS_DATASOURCE_XA_PROPERTY);
//            result = prefixedProperties;
//
//            prefixedProperties.entrySet();
//            result = Collections.singletonMap(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL, "jdbc:oracle:thin:localhost:1521/XE").entrySet();
//        }};
//
//        tomcatDatasourceConfiguration.xaProperties(domibusPropertyProvider);
//
//        new VerificationsInOrder() {{
//            prefixedProperties.setProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_ORACLE_URL, "jdbc:oracle:thin:localhost:1521/XE");
//            prefixedProperties.remove(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL);
//        }};
//    }
//
//    @Test
//    public void xaProperties_doesNotSwitchTheMySQLUrlPropertyKeyToUppercase() {
//        new Expectations() {{
//            new PrefixedProperties(domibusPropertyProvider, DOMIBUS_DATASOURCE_XA_PROPERTY);
//            result = prefixedProperties;
//
//            prefixedProperties.entrySet();
//            result = Collections.singletonMap(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL, "jdbc:mysql://localhost:3306/domibus?pinGlobalTxToPhysicalConnection=true").entrySet();
//        }};
//
//        tomcatDatasourceConfiguration.xaProperties(domibusPropertyProvider);
//
//        new Verifications() {{
//            prefixedProperties.remove(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_URL);
//            times = 0;
//
//            prefixedProperties.setProperty(DOMIBUS_DATASOURCE_XA_PROPERTY_KEY_ORACLE_URL, "jdbc:mysql://localhost:3306/domibus?pinGlobalTxToPhysicalConnection=true");
//            times = 0;
//        }};
//    }
//
//}