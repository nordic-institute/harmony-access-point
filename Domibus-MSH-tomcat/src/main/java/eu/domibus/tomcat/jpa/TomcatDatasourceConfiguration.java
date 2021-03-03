package eu.domibus.tomcat.jpa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.property.PrefixedProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Map;
import java.util.function.Consumer;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class TomcatDatasourceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TomcatDatasourceConfiguration.class);

    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_EM_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public AtomikosNonXADataSourceBean domibusDatasource(DomibusPropertyProvider domibusPropertyProvider) {

        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusDataSource");

        final String driverClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        dataSource.setDriverClassName(driverClassName);
        final String dataSourceURL = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_URL);
        dataSource.setUrl(dataSourceURL);
        final String user = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_USER);
        dataSource.setUser(user);
        final String password = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_PASSWORD); //NOSONAR
        dataSource.setPassword(password);
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetime);

        return dataSource;
    }

    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public AtomikosNonXADataSourceBean quartzDatasource(DomibusPropertyProvider domibusPropertyProvider) {

        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusNonXADataSource");

        final String driverClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        dataSource.setDriverClassName(driverClassName);
        final String dataSourceURL = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_URL);
        dataSource.setUrl(dataSourceURL);
        final String user = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_USER);
        dataSource.setUser(user);
        final String password = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_PASSWORD); //NOSONAR
        dataSource.setPassword(password);
        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetime);

        return dataSource;
    }

}
