package eu.domibus.test.common;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.TimeZone;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusTestDatasourceConfiguration.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Primary
    @Bean(name = {DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE}, destroyMethod = "close")
    public DataSource domibusDatasource() {
        HikariDataSource dataSource = createDataSource();

        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionTimeout(30000);

        return dataSource;
    }

    private HikariDataSource createDataSource() {
        JdbcDataSource h2DataSource = createH2Datasource();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl(h2DataSource.getUrl());
        dataSource.setUsername(h2DataSource.getUser());
        dataSource.setPassword(h2DataSource.getPassword());

        final int maxPoolSize = 20;
        dataSource.setMaximumPoolSize(maxPoolSize);
        final int maxLifetimeInSecs = 10;
        dataSource.setMaxLifetime(maxLifetimeInSecs * 1000L);
        return dataSource;
    }

    private JdbcDataSource createH2Datasource() {
        JdbcDataSource result = new JdbcDataSource();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        final String databaseSchema = domibusPropertyProvider.getProperty(DOMIBUS_DATABASE_SCHEMA);
        final String databaseUrlTemplate = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=3000;INIT=runscript from 'classpath:config/database/create_schema.sql'\\;runscript from 'classpath:config/database/domibus-h2.sql'\\;runscript from 'classpath:config/database/domibus-h2-data.sql'\\;runscript from 'classpath:config/database/schema-h2.sql'";
        String databaseUrl = String.format(databaseUrlTemplate, databaseSchema);

        LOG.info("Using database URL [{}]", databaseUrl);

        result.setUrl(databaseUrl);
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
