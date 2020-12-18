package eu.domibus.test.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Francois Gautier
 * @since 5.0
 */
@EnableTransactionManagement
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDataBaseConfig extends AbstractDatabaseConfig {

    public DataSource dataSource() {
        return getH2DataSource();
    }

    @Override
    public String getSpecificDatabaseDialect() {
        return H_2_DIALECT;
    }

}
