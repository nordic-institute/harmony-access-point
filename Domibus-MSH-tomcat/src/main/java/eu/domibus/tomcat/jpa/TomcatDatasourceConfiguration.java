package eu.domibus.tomcat.jpa;

import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class TomcatDatasourceConfiguration {

    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
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

    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
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
