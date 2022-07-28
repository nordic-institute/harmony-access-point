package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

/**
 * Provides functionality for testing if a domain has a valid database schema{@link DbSchemaUtil}
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class DbSchemaUtilImpl implements DbSchemaUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DbSchemaUtilImpl.class);

    private final EntityManager entityManager;

    private final DomainService domainService;

    private final DomibusConfigurationService domibusConfigurationService;


    public DbSchemaUtilImpl(DomainService domainService,
                            DomibusConfigurationService domibusConfigurationService,
                            EntityManagerFactory entityManagerFactory) {

        this.domainService = domainService;
        this.domibusConfigurationService = domibusConfigurationService;
        entityManager = entityManagerFactory.createEntityManager();
    }

    @Cacheable(value = DomibusCacheService.DOMAIN_VALIDITY_CACHE, sync = true)
    public synchronized boolean isDatabaseSchemaForDomainValid(Domain domain) {

        //in single tenancy the schema validity check is not needed
        if(domibusConfigurationService.isSingleTenantAware()) {
            LOG.info("Domain's database schema validity check is not needed in single tenancy");
            return true;
        }

        if (domain == null) {
            LOG.warn("Domain to be checked is null");
            return false;
        }

        String databaseSchema = null;
        try {
            //set corresponding db schema
            entityManager.getTransaction().begin();
            databaseSchema = domainService.getDatabaseSchema(domain);
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
}
