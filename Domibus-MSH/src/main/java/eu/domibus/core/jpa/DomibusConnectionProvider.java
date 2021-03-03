package eu.domibus.core.jpa;

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
 */
@Conditional(SingleTenantAwareEntityManagerCondition.class)
@Service
public class DomibusConnectionProvider implements ConnectionProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusConnectionProvider.class);

    @Qualifier(DomibusJPAConfiguration.DOMIBUS_JDBC_EM_DATA_SOURCE)
    @Autowired
    protected DataSource dataSource; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    private DatabaseUtil databaseUtil;

    @Override
    public Connection getConnection() throws SQLException {
        LOG.trace("Getting new connection");

        String mdcUser = LOG.getMDC(DomibusLogger.MDC_USER);
        if(StringUtils.isBlank(mdcUser)) {
            String userName = databaseUtil.getDatabaseUserName();
            LOG.putMDC(DomibusLogger.MDC_USER, userName);
        }

        return dataSource.getConnection();
   }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        LOG.trace("Releasing connection");
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
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
