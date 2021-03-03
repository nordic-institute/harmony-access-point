package eu.domibus.core.util;

import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.jpa.DomibusJPAConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides functionality for retrieving the username used to connect to the database.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@DependsOn(DomibusJPAConfiguration.DOMIBUS_JDBC_DATA_SOURCE)
@Service(DatabaseUtil.DATABASE_USER)
public class DatabaseUtilImpl implements DatabaseUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseUtilImpl.class);

    private String databaseUserName;

    @Qualifier(DomibusJPAConfiguration.DOMIBUS_JDBC_DATA_SOURCE)
    @Autowired
    private DataSource dataSource; //NOSONAR: not necessary to be transient or serializable

    @PostConstruct
    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            databaseUserName = connection.getMetaData().getUserName();
            LOG.info("Found database username [{}]", databaseUserName);
        } catch (SQLException e) {
            LOG.error("Could not read the current database username", e);
            throw new IllegalStateException("Could not read the current database username", e);
        }
    }

    public String getDatabaseUserName() {
        return databaseUserName;
    }
}
