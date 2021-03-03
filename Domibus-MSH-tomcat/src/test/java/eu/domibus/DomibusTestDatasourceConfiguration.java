package eu.domibus;

import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestDatasourceConfiguration.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Primary
    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_EM_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public DataSource domibusDatasource() {
        JdbcDataSource h2DataSource = createDatasource("domibusDataSource");

        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusDataSource");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(h2DataSource.getUrl());
        dataSource.setPassword(h2DataSource.getPassword());
        dataSource.setUser(h2DataSource.getUser());

        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetime);

        dataSource.setMaxIdleTime(60);
        dataSource.setMaintenanceInterval(60);
        dataSource.setReapTimeout(0);
        dataSource.setBorrowConnectionTimeout(30);

        LOG.info("domibusDatasource bean created with url [{}]", dataSource.getUrl());
        return dataSource;
    }

    @Primary
    @Bean(name = DomibusJPAConfiguration.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, initMethod = "init", destroyMethod = "close")
    public DataSource quartzDatasource() {
        JdbcDataSource h2DataSource = createDatasource("domibusNonXADataSource");

        AtomikosNonXADataSourceBean dataSource = new AtomikosNonXADataSourceBean();
        dataSource.setUniqueResourceName("domibusNonXADataSource");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(h2DataSource.getUrl());
        dataSource.setPassword(h2DataSource.getPassword());
        dataSource.setUser(h2DataSource.getUser());

        final Integer minPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MIN_POOL_SIZE);
        dataSource.setMinPoolSize(minPoolSize);
        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaxPoolSize(maxPoolSize);
        final Integer maxLifetime = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetime);

        LOG.info("quartzDatasource bean created with url [{}]", dataSource.getUrl());
        return dataSource;
    }

    private JdbcDataSource createDatasource(String name) {
        JdbcDataSource result = new JdbcDataSource();
        result.setUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
