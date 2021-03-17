package eu.domibus.tomcat.jpa;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class TomcatDatasourceConfiguration {

    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, destroyMethod = "close")
    public DataSource domibusDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        return getHikariDataSource(domibusPropertyProvider);
    }

    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, destroyMethod = "close")
    public DataSource quartzDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        return getHikariDataSource(domibusPropertyProvider);
    }

    private HikariDataSource getHikariDataSource(DomibusPropertyProvider domibusPropertyProvider) {
        HikariDataSource dataSource = new HikariDataSource();

        final String driverClassName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        dataSource.setDriverClassName(driverClassName);

        final String dataSourceURL = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_URL);
        dataSource.setJdbcUrl(dataSourceURL);

        final String user = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_USER);
        dataSource.setUsername(user);

        final String password = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_PASSWORD); //NOSONAR
        dataSource.setPassword(password);

        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaximumPoolSize(maxPoolSize);

        final Integer maxLifetimeInSecs = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetimeInSecs * 1000);

        return dataSource;
    }

}
