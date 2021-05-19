package eu.domibus;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, destroyMethod = "close")
    public DataSource domibusDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        HikariDataSource dataSource = createDataSource(domibusPropertyProvider);

        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionTimeout(30000);

        return dataSource;
    }

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_NON_XA_DATA_SOURCE, destroyMethod = "close")
    @DependsOn(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    public DataSource quartzDatasource(DomibusPropertyProvider domibusPropertyProvider) {
        HikariDataSource dataSource = createDataSource(domibusPropertyProvider);
        return dataSource;
    }

    private HikariDataSource createDataSource(DomibusPropertyProvider domibusPropertyProvider) {
        JdbcDataSource h2DataSource = createH2Datasource();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl(h2DataSource.getUrl());
        dataSource.setUsername(h2DataSource.getUser());
        dataSource.setPassword(h2DataSource.getPassword());

        final Integer maxPoolSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_POOL_SIZE);
        dataSource.setMaximumPoolSize(maxPoolSize);
        final Integer maxLifetimeInSecs = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DATASOURCE_MAX_LIFETIME);
        dataSource.setMaxLifetime(maxLifetimeInSecs * 1000);
        return dataSource;
    }

    private JdbcDataSource createH2Datasource() {
        JdbcDataSource result = new JdbcDataSource();
        result.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=runscript from 'classpath:dataset/database/create_schema.sql'\\;runscript from 'classpath:h2.sql'");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
