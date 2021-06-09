package eu.domibus.test.common;

import com.zaxxer.hikari.HikariDataSource;
import eu.domibus.api.datasource.DataSourceConstants;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.TimeZone;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class DomibusTestDatasourceConfiguration {

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE, destroyMethod = "close")
    public DataSource domibusDatasource() {
        HikariDataSource dataSource = createDataSource();

        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionTimeout(30000);

        return dataSource;
    }

    @Primary
    @Bean(name = DataSourceConstants.DOMIBUS_JDBC_QUARTZ_DATA_SOURCE, destroyMethod = "close")
    @DependsOn(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    public DataSource quartzDatasource() {
        return createDataSource();
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
        result.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;INIT=runscript from 'classpath:config/database/create_schema.sql'\\;runscript from 'classpath:config/database/domibus-h2.sql'\\;runscript from 'classpath:config/database/domibus-h2-data.sql'\\;runscript from 'classpath:config/database/schema-h2.sql'");
        result.setUser("sa");
        result.setPassword("");
        return result;
    }
}
