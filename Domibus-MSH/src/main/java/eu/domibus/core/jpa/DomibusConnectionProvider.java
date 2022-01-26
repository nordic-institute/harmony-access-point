package eu.domibus.core.jpa;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.2
 *
 * Transaction Isolation set to {@value Connection#TRANSACTION_READ_COMMITTED}
 */
@Conditional(SingleTenantAwareEntityManagerCondition.class)
@Service
public class DomibusConnectionProvider implements ConnectionProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusConnectionProvider.class);

    @Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE)
    @Autowired
    protected DataSource dataSource; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    private DatabaseUtil databaseUtil;

    @Override
    public Connection getConnection() throws SQLException {
        LOG.trace("Getting new connection");

        return getDBConnection();
    }

    protected Connection getDBConnection() throws SQLException {
        String mdcUser = LOG.getMDC(DomibusLogger.MDC_USER);
        if (StringUtils.isBlank(mdcUser)) {
            String userName = databaseUtil.getDatabaseUserName();
            LOG.putMDC(DomibusLogger.MDC_USER, userName);
        }
        Connection connection = null;
        try {
            dataSource.getConnection();
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Transaction Isolation set to [{}] on [{}]", Connection.TRANSACTION_READ_COMMITTED, connection.getClass());
                LOG.trace("Auto Commit set to [{}]", connection.getAutoCommit());
            }
        } catch (SQLException ex) {
            LOG.info("Cannot establish a connection with the data source.", ex);
        } finally {
            connection.close();
        }
        return connection;
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        LOG.trace("Releasing connection");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }
}
