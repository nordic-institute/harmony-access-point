package eu.domibus.tomcat.jpa;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.PrefixedProperties;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static eu.domibus.tomcat.jpa.TomcatDatasourceConfiguration.DOMIBUS_DATASOURCE_XA_PROPERTY;

@RunWith(JMockit.class)
public class TomcatDatasourceConfigurationTest {

    @Tested
    private TomcatDatasourceConfiguration tomcatDatasourceConfiguration;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Mocked
    private PrefixedProperties prefixedProperties;

    @Test
    public void xaProperties_switchesTheOracleUrlPropertyKeyToUppercase() {
        new Expectations() {{
            new PrefixedProperties(domibusPropertyProvider, DOMIBUS_DATASOURCE_XA_PROPERTY);
            result = prefixedProperties;

            prefixedProperties.entrySet();
            result = Collections.singletonMap("domibus.datasource.xa.property.url", "jdbc:oracle:thin:localhost:1521/XE").entrySet();
        }};

        tomcatDatasourceConfiguration.xaProperties(domibusPropertyProvider);

        new Verifications() {{
            prefixedProperties.remove("domibus.datasource.xa.property.url");
            prefixedProperties.setProperty("domibus.datasource.xa.property.URL", "jdbc:oracle:thin:localhost:1521/XE");
        }};
    }

    @Test
    public void xaProperties_doesNotSwitchTheMySQLUrlPropertyKeyToUppercase() {
        new Expectations() {{
            new PrefixedProperties(domibusPropertyProvider, DOMIBUS_DATASOURCE_XA_PROPERTY);
            result = prefixedProperties;

            prefixedProperties.entrySet();
            result = Collections.singletonMap("domibus.datasource.xa.property.url", "jdbc:mysql://localhost:3306/domibus?pinGlobalTxToPhysicalConnection=true").entrySet();
        }};

        tomcatDatasourceConfiguration.xaProperties(domibusPropertyProvider);

        new Verifications() {{
            prefixedProperties.remove("domibus.datasource.xa.property.url");
            times = 0;

            prefixedProperties.setProperty("domibus.datasource.xa.property.URL", "jdbc:mysql://localhost:3306/domibus?pinGlobalTxToPhysicalConnection=true");
            times = 0;
        }};
    }

}