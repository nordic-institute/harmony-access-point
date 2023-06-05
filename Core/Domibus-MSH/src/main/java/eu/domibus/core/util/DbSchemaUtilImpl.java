package eu.domibus.core.util;

import eu.domibus.api.datasource.DataSourceConstants;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.api.util.DomibusDatabaseNotSupportedException;
import eu.domibus.api.util.FaultyDatabaseSchemaNameException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
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
import java.util.concurrent.*;

import static eu.domibus.api.model.UserMessage.DEFAULT_USER_MESSAGE_ID_PK;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;
import static eu.domibus.common.TaskExecutorConstants.DOMIBUS_TASK_EXECUTOR_BEAN_NAME;
import static eu.domibus.core.multitenancy.DomainTaskExecutorImpl.DEFAULT_WAIT_TIMEOUT_IN_SECONDS;

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

    protected final SchedulingTaskExecutor schedulingTaskExecutor;

    public DbSchemaUtilImpl(@Qualifier(DataSourceConstants.DOMIBUS_JDBC_DATA_SOURCE) DataSource dataSource,
                            DomibusConfigurationService domibusConfigurationService,
                            DomibusPropertyProvider domibusPropertyProvider,
                            @Qualifier(DOMIBUS_TASK_EXECUTOR_BEAN_NAME) SchedulingTaskExecutor schedulingTaskExecutor) {
        this.dataSource = dataSource;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.schedulingTaskExecutor = schedulingTaskExecutor;
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

        return executeOnNewThread(() -> {
            return doIsDatabaseSchemaForDomainValid(domain);
        }, domain);
    }

    protected Boolean doIsDatabaseSchemaForDomainValid(Domain domain) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            String databaseSchema = getDatabaseSchema(domain);

            try {
                setSchema(connection, databaseSchema);
            } catch (PersistenceException | FaultyDatabaseSchemaNameException e) {
                LOG.warn("Could not set database schema [{}] for domain [{}], so it is not a proper schema.", databaseSchema, domain.getCode());
                return false;
            }

            try {
                checkTableExists(databaseSchema, connection);
                LOG.trace("Found table TB_USER_MESSAGE for domain [{}], so it is a proper schema.", domain.getCode());
                return true;
            } catch (final Exception e) {
                LOG.warn("Could not find table TB_USER_MESSAGE for domain [{}], so it is not a proper schema.", domain.getCode());
                return false;
            }

        } catch (SQLException e) {
            LOG.warn("Could not create a connection for domain [{}].", domain);
            return false;
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

    @Override
    public boolean isDatabaseSchemaNameSane(final String schemaName) {
        return schemaName.matches(ALPHANUMERIC_PATTERN_WITH_UNDERSCORE);
    }

    private void checkTableExists(String databaseSchema, Connection connection) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            String sql = getCheckTableExistsSql(databaseSchema);
            statement.execute(sql);
            LOG.trace("Executed statement [{}] for schema [{}]", sql, databaseSchema);
        }
    }

    private String getCheckTableExistsSql(String databaseSchemaName) {
        final DataBaseEngine databaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result;

        if (!isDatabaseSchemaNameSane(databaseSchemaName)) {
            LOG.error("Faulty database schema name: [{}]", databaseSchemaName);
            throw new FaultyDatabaseSchemaNameException("Database schema name is invalid: " + databaseSchemaName);
        }

        switch (databaseEngine) {
            case MYSQL:
            case H2:
            case ORACLE:
                result = "SELECT ID_PK FROM " + databaseSchemaName + ".TB_USER_MESSAGE WHERE ID_PK=" + DEFAULT_USER_MESSAGE_ID_PK;
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

    protected <T extends Object> T executeOnNewThread(Callable<T> task, Domain domain) {
        DomainCallable<T> domainCallable = new DomainCallable<>(task, domain);
        final Future<T> utrFuture = schedulingTaskExecutor.submit(domainCallable);
        try {
            return utrFuture.get(DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Restore interrupted state
            Thread.currentThread().interrupt();
            throw new DomainTaskException("Could not execute task", e);
        }
    }

    static class DomainCallable<T> implements Callable<T> {

        protected Callable<T> callable;
        protected Domain domain;

        public DomainCallable(Callable<T> callable, Domain domain) {
            this.callable = callable;
            this.domain = domain;
        }

        @Override
        public T call() throws Exception {
            if (domain == null) {
                LOG.removeMDC(DomibusLogger.MDC_DOMAIN);
            } else {
                LOG.putMDC(DomibusLogger.MDC_DOMAIN, domain.getCode());
            }
            return callable.call();
        }
    }

}
