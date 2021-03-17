package eu.domibus.tomcat.jpa;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.StringUtils;
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

    public static final int MILISECS_IN_SEC = 1000;

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

        final Integer maxLifetimeInSecs = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetimeInSecs * MILISECS_IN_SEC);

        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaximumPoolSize(maxPoolSize);

        final boolean autoCommit = domibusPropertyProvider.getBooleanProperty(DOMIBUS_DATASOURCE_AUTO_COMMIT);
        dataSource.setAutoCommit(autoCommit);

        final Integer connectionTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_CONNECTION_TIMEOUT);
        dataSource.setConnectionTimeout(connectionTimeout * MILISECS_IN_SEC);

        final Integer idleTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_IDLE_TIMEOUT);
        dataSource.setIdleTimeout(idleTimeout * MILISECS_IN_SEC);

        final Integer minimumIdle = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MINIMUM_IDLE);
        dataSource.setMinimumIdle(minimumIdle * MILISECS_IN_SEC);

        final String poolName = domibusPropertyProvider.getProperty(DOMIBUS_DATASOURCE_POOL_NAME);
        if (!StringUtils.isBlank(poolName)) {
            dataSource.setPoolName(poolName);
        }

        return dataSource;
    }

}
