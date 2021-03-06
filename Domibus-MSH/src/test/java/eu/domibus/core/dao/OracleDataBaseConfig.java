package eu.domibus.core.dao;

import eu.domibus.core.audit.AuditIT;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Utility class that allows to run some test directly on you local database while developing you tests, instead of using
 * H2 brower utility. When your test is working switch the profile to @ActiveProfiles("IN_MEMORY_DATABASE")
 * lik in {@link AuditIT}
 */
@EnableTransactionManagement
@Profile("ORACLE_DATABASE")
public class OracleDataBaseConfig extends AbstractDatabaseConfig {

    public DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        driverManagerDataSource.setUrl("");
        driverManagerDataSource.setUsername("sa");
        driverManagerDataSource.setPassword("");
        return driverManagerDataSource;
    }

    public Map<Object, Object> getProperties() {
        Map<Object, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect","org.hibernate.dialect.Oracle10gDialect");
        return properties;
    }

}
