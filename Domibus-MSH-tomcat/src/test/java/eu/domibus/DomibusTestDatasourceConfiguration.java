package eu.domibus;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_MAX_LIFETIME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Primary
    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public DataSource xaDatasource() {
        JdbcDataSource h2DataSource = createDatasource();

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setUniqueResourceName("domibusJDBC-XA");
        xaDataSource.setXaDataSource(h2DataSource);

        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE);
        xaDataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE);
        xaDataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifeTime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_LIFETIME);
        xaDataSource.setMaxLifetime(maxLifeTime);

        final Integer borrowConnectionTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT);
        xaDataSource.setBorrowConnectionTimeout(borrowConnectionTimeout);
        final Integer reapTimeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT);
        xaDataSource.setReapTimeout(reapTimeout);
        final Integer maxIdleTime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME);
        xaDataSource.setMaxIdleTime(maxIdleTime);
        final Integer maintenanceInterval = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL);
        xaDataSource.setMaintenanceInterval(maintenanceInterval);

        return xaDataSource;
    }

    @Primary
    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public DataSource nonXADatasource() {
        JdbcDataSource h2DataSource = createDatasource();

        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusNonXADataSource");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(h2DataSource.getUrl());
        dataSource.setPassword(h2DataSource.getPassword());
        dataSource.setUser(h2DataSource.getUser());
        return dataSource;
    }

    protected JdbcDataSource createDatasource() {
        JdbcDataSource result = new JdbcDataSource();
        result.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
