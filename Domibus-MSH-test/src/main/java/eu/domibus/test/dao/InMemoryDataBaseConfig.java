package eu.domibus.test.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.TimeZone;

/**
 * @author Francois Gautier
 * @since 5.0
 */
//TODO to be replaced by AbstractIT
@EnableTransactionManagement(proxyTargetClass = true)
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDataBaseConfig extends AbstractDatabaseConfig {

    public DataSource dataSource() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return getH2DataSource();
    }

    @Override
    public String getSpecificDatabaseDialect() {
        return H_2_DIALECT;
    }

}
