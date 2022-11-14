package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;

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

    protected volatile Map<Domain, String> domainSchemas = new HashMap<>();

    protected final Object generalSchemaLock = new Object();

    protected volatile String generalSchema;

    private EntityManager entityManager;

    private final EntityManagerFactory entityManagerFactory;

    private final DomibusConfigurationService domibusConfigurationService;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public DbSchemaUtilImpl(@Lazy EntityManagerFactory entityManagerFactory,
                            DomibusConfigurationService domibusConfigurationService,
                            DomibusPropertyProvider domibusPropertyProvider) {
        this.entityManagerFactory = entityManagerFactory;

        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * Get database schema name for the domain. Uses a local cache. This mechanism should be removed when EDELIVERY-7353 it will be implemented
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
                    String value = domibusPropertyProvider.getProperty(domain, DOMIBUS_DATABASE_SCHEMA);
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
     * Get the configured general schema. Uses a local cache. This mechanism should be removed when EDELIVERY-7353 it will be implemented
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

    @Cacheable(value = DomibusCacheService.DOMAIN_VALIDITY_CACHE, sync = true)
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

        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        }
        String databaseSchema = null;
        try {
            //set corresponding db schema
            entityManager.getTransaction().begin();
            databaseSchema = getDatabaseSchema(domain);
            String schemaChangeSQL = getSchemaChangeSQL(databaseSchema);
            Query q = entityManager.createNativeQuery(schemaChangeSQL);
            //check if the domain's database schema can be accessed
            q.executeUpdate();

            return true;
        } catch (PersistenceException e) {
            LOG.warn("Could not set database schema [{}] for domain [{}]", databaseSchema, domain.getCode());
            return false;
        } finally {
            //revert changing of the current schema
            entityManager.getTransaction().rollback();
        }
    }

    public String getSchemaChangeSQL(String databaseSchema) {
        final DataBaseEngine databaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result;

        switch (databaseEngine) {
            case MYSQL:
                result = "USE " + databaseSchema;
                break;
            case H2:
                result = "SET SCHEMA " + databaseSchema;
                break;
            case ORACLE:
                result = "ALTER SESSION SET CURRENT_SCHEMA = " + databaseSchema;
                break;
            default:
                LOG.error("Unsupported database engine: {}", databaseEngine);
                throw new DomibusDatabaseNotSupportedException("Unsupported database engine ...");
        }

        LOG.debug("Generated SQL string for changing the schema: {}", result);

        return result;
    }

    @Override
    public void removeDatabaseSchema(Domain domain) {
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
}
