package eu.domibus.core.util;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.api.util.DomibusDatabaseNotSupportedException;
import eu.domibus.api.util.FaultyDatabaseSchemaNameException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * Provides functionality for testing if a domain has a valid database schema{@link DbSchemaUtil}
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class DbSchemaUtilImpl implements DbSchemaUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DbSchemaUtilImpl.class);

    private static final String ALPHANUMERIC_PATTERN_WITH_UNDERSCORE = "^[a-zA-Z0-9_]+$";

    protected volatile Map<Domain, String> domainSchemas = new HashMap<>();

    protected final Object generalSchemaLock = new Object();

    protected volatile String generalSchema;

    private final DomibusConfigurationService domibusConfigurationService;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DataSource dataSource;

    public DbSchemaUtilImpl(@Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE) DataSource dataSource,
                            DomibusConfigurationService domibusConfigurationService,
                            DomibusPropertyProvider domibusPropertyProvider) {
        this.dataSource = dataSource;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * Get database schema name for the domain. Uses a local cache.
     *
     * @param domain the domain for which the db schema is retrieved
     * @return database schema name
     */
    @Override
    public String getDatabaseSchema(Domain domain) {
        String domainSchema = domainSchemas.get(domain);
        if (domainSchema == null) {
            synchronized (domainSchemas) {
                domainSchema = domainSchemas.get(domain);
                if (domainSchema == null) {
                    String value = getDBSchemaFromPropertyFile(domain);
                    if (value == null) {
                        LOG.warn("Database schema for domain [{}] was null, removing from cache", domain);
                        domainSchemas.remove(domain);
                    } else {
                        LOG.debug("Caching domain schema [{}] for domain [{}]", value, domain);
                        domainSchemas.put(domain, value);
                    }
                    domainSchema = value;
                }
            }
        }

        return domainSchema;
    }

    /**
     * Get the configured general schema. Uses a local cache.
     */
    @Override
    public String getGeneralSchema() {
        if (generalSchema == null) {
            synchronized (generalSchemaLock) {
                if (generalSchema == null) {
                    generalSchema = domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
                    LOG.debug("Caching general schema [{}]", generalSchema);
                }
            }
        }
        return generalSchema;
    }

    @Override
    public synchronized boolean isDatabaseSchemaForDomainValid(Domain domain) {
        //in single tenancy the schema validity check is not needed
        if (domibusConfigurationService.isSingleTenantAware()) {
            LOG.info("Domain's database schema validity check is not needed in single tenancy");
            return true;
        }

        if (domain == null) {
            LOG.warn("Domain to be checked is null");
            return false;
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOG.warn("Could not create a connection.");
            return true;
        }

        String databaseSchema = getDatabaseSchema(domain);
        try {
            setSchema(connection, databaseSchema);
        } catch (PersistenceException | FaultyDatabaseSchemaNameException e) {
            LOG.warn("Could not set database schema [{}] for domain [{}], so it is not a proper schema.", databaseSchema, domain.getCode());
            return false;
        }

        try {
            tryCreateExistingTable(databaseSchema, connection);
            LOG.warn("Could create table TB_USER_MESSAGE for domain [{}], so it is not a proper schema.", domain.getCode());
            return false;
        } catch (final SQLException e) {
            LOG.trace("Could not create table TB_USER_MESSAGE for domain [{}], so it is a proper schema.", domain.getCode());
            return true;
        }
    }

    @Override
    public void setSchema(final Connection connection, String databaseSchema) {
        try {
            try (final Statement statement = connection.createStatement()) {
                final String schemaChangeSQL = getSchemaChangeSQL(databaseSchema);
                LOG.trace("Change current schema:[{}]", schemaChangeSQL);
                statement.execute(schemaChangeSQL);
            }
        } catch (final SQLException e) {
            throw new FaultyDatabaseSchemaNameException("Could not alter JDBC connection to specified schema [" + databaseSchema + "]", e);
        }
    }

    @Override
    public void removeCachedDatabaseSchema(Domain domain) {
        String domainSchema = domainSchemas.get(domain);
        if (domainSchema == null) {
            LOG.debug("Domain schema for domain [{}] not found; exiting", domain);
            return;
        }
        synchronized (domainSchemas) {
            domainSchema = domainSchemas.get(domain);
            if (domainSchema != null) {
                LOG.debug("Removing domain schema [{}] for domain [{}]", domainSchema, domain);
                domainSchemas.remove(domain);
            }
        }
    }

    @Override
    public String getSchemaChangeSQL(String databaseSchemaName) throws DomibusDatabaseNotSupportedException, FaultyDatabaseSchemaNameException {
        final DataBaseEngine databaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result;

        if (!isDatabaseSchemaNameSane(databaseSchemaName)) {
            LOG.error("Faulty database schema name: [{}]", databaseSchemaName);
            throw new FaultyDatabaseSchemaNameException("Database schema name is invalid: " + databaseSchemaName);
        }

        switch (databaseEngine) {
            case MYSQL:
                result = "USE " + databaseSchemaName;
                break;
            case H2:
                result = "SET SCHEMA " + databaseSchemaName;
                break;
            case ORACLE:
                result = "ALTER SESSION SET CURRENT_SCHEMA = " + databaseSchemaName;
                break;
            default:
                LOG.error("Unsupported database engine: {}", databaseEngine);
                throw new DomibusDatabaseNotSupportedException("Unsupported database engine: [" + databaseEngine + "]");
        }

        LOG.debug("Generated SQL string for changing the schema: {}", result);

        return result;
    }

    private void tryCreateExistingTable(String databaseSchema, Connection connection) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final String sql = getCreateTableSql(databaseSchema);
            LOG.trace("Checking if a table exists: [{}]", sql);
            statement.execute(sql);
            statement.execute("DROP TABLE " + databaseSchema + ".TB_USER_MESSAGE");
            LOG.trace("Dropping table exists: [{}]", sql);
        }
    }

    private String getCreateTableSql(String databaseSchemaName) {
        final DataBaseEngine databaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result;

        if (!isDatabaseSchemaNameSane(databaseSchemaName)) {
            LOG.error("Faulty database schema name: [{}]", databaseSchemaName);
            throw new FaultyDatabaseSchemaNameException("Database schema name is invalid: " + databaseSchemaName);
        }

        switch (databaseEngine) {
            case MYSQL:
            case H2:
                result = "CREATE TABLE " + databaseSchemaName + ".TB_USER_MESSAGE (ID INT)";
                break;
            case ORACLE:
                result = "CREATE TABLE " + databaseSchemaName + ".TB_USER_MESSAGE (ID number)";
                break;
            default:
                LOG.error("Unsupported database engine: {}", databaseEngine);
                throw new DomibusDatabaseNotSupportedException("Unsupported database engine: [" + databaseEngine + "]");
        }

        LOG.debug("Generated SQL string for changing the schema: {}", result);

        return result;
    }

    protected String getDBSchemaFromPropertyFile(Domain domain) {
        if (domibusConfigurationService.isSingleTenantAware()) {
            return domibusPropertyProvider.getProperty(domain, DOMIBUS_DATABASE_SCHEMA);
        }

        if (domain == null) {
            LOG.warn("Cannot get the database schema name since the domain provided is null.");
            return null;
        }

        String propertiesFilePath = domibusConfigurationService.getConfigLocation() + File.separator
                + domibusConfigurationService.getConfigurationFileName(domain);
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) {
            Properties properties = new Properties();
            properties.load(fis);
            return properties.getProperty(domain.getCode() + "." + DOMIBUS_DATABASE_SCHEMA);
        } catch (IOException ex) {
            LOG.warn("Could not properties from file [{}] to get the database schema name.", propertiesFilePath);
            return null;
        }
    }

    @Override
    public boolean isDatabaseSchemaNameSane(final String schemaName) {
        return schemaName.matches(ALPHANUMERIC_PATTERN_WITH_UNDERSCORE);
    }
}
