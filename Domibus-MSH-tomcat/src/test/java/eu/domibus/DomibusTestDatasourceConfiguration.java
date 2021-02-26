package eu.domibus;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
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

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

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
        dataSource.setMaxPoolSize(100);
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
