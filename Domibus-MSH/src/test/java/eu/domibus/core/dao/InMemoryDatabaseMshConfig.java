package eu.domibus.core.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@EnableTransactionManagement
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDatabaseMshConfig extends AbstractDatabaseMshConfig {

    public DataSource dataSource() {
        return getH2DataSource();
    }

    @Override
    public String getSpecificDatabaseDialect() {
        return H_2_DIALECT;
    }

}
