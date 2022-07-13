package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.*;

/**
 * Provides functionality for testing if a domain has a valid database schema{@link DbSchemaUtil}
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Service
public class DbSchemaUtilImpl implements DbSchemaUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DbSchemaUtilImpl.class);

    @PersistenceUnit(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManagerFactory entityManagerFactory;

    private final DomainService domainService;

    private final DomibusConfigurationService domibusConfigurationService;

    private EntityManager entityManager;

    public DbSchemaUtilImpl(DomainService domainService,
                            DomibusConfigurationService domibusConfigurationService,
                            EntityManagerFactory entityManagerFactory) {

        this.domainService = domainService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.entityManagerFactory = entityManagerFactory;
    }

    public boolean isDatabaseSchemaForDomainValid(Domain domain) {
        if (domain == null) {
            LOG.warn("Domain to be checked is null");
            return false;
        }

        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        }

        try {
            //set corresponding db schema
            entityManager.getTransaction().begin();
            String databaseSchema = domainService.getDatabaseSchema(domain);
            String schemaChangeSQL = getSchemaChangeSQL(databaseSchema);
            Query q = entityManager.createNativeQuery(schemaChangeSQL);
            q.executeUpdate();
            entityManager.getTransaction().commit();

            return true;
        } catch (PersistenceException e) {
            LOG.warn("Could not set schema for domain [{}]", domain.getCode());
            entityManager.getTransaction().rollback();
            return false;
        }
    }

    public String getSchemaChangeSQL(String databaseSchema) {
        final DataBaseEngine dataBaseEngine = domibusConfigurationService.getDataBaseEngine();
        String result = "USE " + databaseSchema;
        if (DataBaseEngine.ORACLE == dataBaseEngine) {
            result = "ALTER SESSION SET CURRENT_SCHEMA = " + databaseSchema;
        }
        return result;
    }
}
