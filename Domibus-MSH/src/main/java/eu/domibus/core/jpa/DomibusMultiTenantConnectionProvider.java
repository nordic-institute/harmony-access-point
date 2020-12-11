package eu.domibus.core.jpa;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_GENERAL_SCHEMA;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Conditional(MultiTenantAwareEntityManagerCondition.class)
@Service
public class DomibusMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMultiTenantConnectionProvider.class);

    @Qualifier(DomibusJPAConfiguration.DOMIBUS_JDBC_XA_DATA_SOURCE)
    @Autowired
    protected DataSource dataSource; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    protected DomainContextProvider domainContextProvider; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    protected DomainService domainService; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider; //NOSONAR: not necessary to be transient or serializable

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DatabaseUtil databaseUtil;

    @Override
    public Connection getAnyConnection() throws SQLException {
        LOG.trace("Getting any connection");

        String mdcUser = LOG.getMDC(DomibusLogger.MDC_USER);
        if(StringUtils.isBlank(mdcUser)) {
            String userName = databaseUtil.getDatabaseUserName();
            LOG.putMDC(DomibusLogger.MDC_USER, userName);
        }

        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String identifier) throws SQLException {
        final Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        String databaseSchema;
        if (currentDomain != null) {
            LOG.trace("Getting schema for domain [{}]", currentDomain);
            databaseSchema = domainService.getDatabaseSchema(currentDomain);
            LOG.trace("Found database schema name as [{}] for current domain [{}]", databaseSchema, currentDomain);
        } else {
            LOG.trace("Getting general schema");
            databaseSchema = domainService.getGeneralSchema();
            LOG.trace("Database schema name for general schema: [{}]", databaseSchema);
        }

        final Connection connection = getAnyConnection();
        LOG.trace("Setting database schema to [{}] ", databaseSchema);

        verifyDatabaseSchema(currentDomain, databaseSchema);

        setSchema(connection, databaseSchema);
        return connection;
    }

    protected void verifyDatabaseSchema(Domain currentDomain, String databaseSchema) {
        if (currentDomain != null && StringUtils.isEmpty(databaseSchema)) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Database domain schema name not found for the domain:" + currentDomain + " and for the property:" + currentDomain + "." + DOMIBUS_DATABASE_SCHEMA);
        }
        if (StringUtils.isEmpty(databaseSchema)) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Database schema name not found for general schema and for the property:" + DOMIBUS_DATABASE_GENERAL_SCHEMA);
        }
    }

    protected void setSchema(final Connection connection, String databaseSchema) throws SQLException {
        try {
            try (final Statement statement = connection.createStatement()) {
                final String schemaChangeSQL = getSchemaChangeSQL(databaseSchema);
                LOG.trace("Change current schema:[{}]",schemaChangeSQL);
                statement.execute(schemaChangeSQL);
            }
        } catch (final SQLException e) {
            throw new HibernateException("Could not alter JDBC connection to specified schema [" + databaseSchema + "]", e);
        }
    }

    protected String getSchemaChangeSQL(String databaseSchema) {
        final DataBaseEngine dataBaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result = "USE " + databaseSchema;
        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            result = "ALTER SESSION SET CURRENT_SCHEMA = " + databaseSchema;
        }
        return result;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        final String generalSchema = domainService.getGeneralSchema();
        LOG.trace("Releasing connection, setting database schema to [{}] ", generalSchema);
        setSchema(connection, generalSchema);
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
