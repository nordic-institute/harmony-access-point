package eu.domibus.core.util;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.user.ui.User;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Provides functionality for testing if a domain has a valid database schema
 *
 * @author Lucian FURCA
 * @since 5.1
 */
@Component
public class DbSchemaUtilImpl implements DbSchemaUtil {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DbSchemaUtilImpl.class);

    @Autowired
    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private final DomainService domainService;

    private final DomibusConfigurationService domibusConfigurationService;

    public DbSchemaUtilImpl(DomainService domainService, DomibusConfigurationService domibusConfigurationService) {
        this.domainService = domainService;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    public boolean isDatabaseSchemaForDomainValid(Domain domain) {
        if (domain == null) {
            LOG.warn("Domain to be checked is null");
            return false;
        }
        TypedQuery<User> namedQueryForTest = entityManager.createNamedQuery("User.findAll", User.class);

        try {
            //set corresponding db schema
            String databaseSchema = domainService.getDatabaseSchema(domain);
            String schemaChangeSQL = getSchemaChangeSQL(databaseSchema);
            Query q = entityManager.createNativeQuery(schemaChangeSQL);
            q.executeUpdate();
            namedQueryForTest.getResultList();
        } catch (Exception e) {
            LOG.warn("Could not set schema for domain [{}]", domain.getCode());
            return false;
        }
        return true;
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
