package eu.domibus.weblogic.jpa;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class WebLogicDatasourceConfigurationTest {

    @Tested
    WebLogicDatasourceConfiguration webLogicDatasourceConfiguration;

    @Test
    public void xaDatasource(@Injectable  DomibusPropertyProvider domibusPropertyProvider,
                             @Mocked JndiObjectFactoryBean jndiObjectFactoryBean) {
        String jndiName = "jndi/datasource";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_JNDI_NAME);
            this.result = jndiName;
        }};

        webLogicDatasourceConfiguration.xaDatasource(domibusPropertyProvider);

        new Verifications() {{
            jndiObjectFactoryBean.setExpectedType(DataSource.class);
            jndiObjectFactoryBean.setJndiName(jndiName);
        }};
    }

    @Test
    public void quartzDatasource(@Injectable  DomibusPropertyProvider domibusPropertyProvider,
                                 @Mocked JndiObjectFactoryBean jndiObjectFactoryBean) {
        String jndiName = "jndi/datasource";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME);
            this.result = jndiName;
        }};

        webLogicDatasourceConfiguration.quartzDatasource(domibusPropertyProvider);

        new Verifications() {{
            jndiObjectFactoryBean.setExpectedType(DataSource.class);
            jndiObjectFactoryBean.setJndiName(jndiName);
        }};
    }
}