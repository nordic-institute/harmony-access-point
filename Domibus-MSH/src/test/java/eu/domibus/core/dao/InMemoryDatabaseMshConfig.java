package eu.domibus.core.dao;

import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.TimeZone;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Profile("IN_MEMORY_DATABASE")
public class InMemoryDatabaseMshConfig extends AbstractDatabaseMshConfig {

    public DataSource dataSource() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return getH2DataSource();
    }


    @Override
    public String getSpecificDatabaseDialect() {
        return H_2_DIALECT;
    }

}
