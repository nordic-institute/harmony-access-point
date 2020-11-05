package eu.domibus.core.dao;

import eu.domibus.core.audit.AuditIT;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


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
public class OracleDataBaseConfig extends AbstractDatabaseMshConfig {

    public DataSource dataSource() {
        return getOracleDataSource();
    }

    @Override
    public String getSpecificDatabaseDialect() {
        return ORACLE_DIALECT;
    }

}
