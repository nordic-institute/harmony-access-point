package eu.domibus.core.dao;

import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.jpa.DomibusConnectionProvider;
import eu.domibus.core.util.DatabaseUtilImpl;
import eu.domibus.test.dao.AbstractDatabaseConfig;
import mockit.Deencapsulation;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public abstract class AbstractDatabaseMshConfig extends AbstractDatabaseConfig {

    @Autowired
    private DatabaseUtil databaseUtil;

    @Bean
    public ConnectionProvider connectionProvider(DataSource dataSource) {
        DomibusConnectionProvider domibusConnectionProvider = new DomibusConnectionProvider();
        Deencapsulation.setField(domibusConnectionProvider, "dataSource", dataSource);
        Deencapsulation.setField(domibusConnectionProvider, "databaseUtil", databaseUtil);
        return domibusConnectionProvider;
    }

    @Bean
    public DatabaseUtil databaseUtil(DataSource dataSource) {
        DatabaseUtilImpl databaseUtil = new DatabaseUtilImpl();
        Deencapsulation.setField(databaseUtil, "dataSource", dataSource);
        databaseUtil.init();
        return databaseUtil;
    }

}
