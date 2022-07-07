package eu.domibus.api.util;

import eu.domibus.api.multitenancy.Domain;

/**
 * Provides functionality for testing if a domain has a valid database schema
 *
 * @author Lucian FURCA
 * @since 5.1
 */
public interface DbSchemaUtil {

    /**
     * Checks if the database schema associated to the domain can be accessed
     * @param domain
     * @return
     */
    boolean isDatabaseSchemaForDomainValid(Domain domain);

    /**
     * Create SQL command for changing the schema
     * @param databaseSchema
     * @return sql command string
     */
    String getSchemaChangeSQL(String databaseSchema);
}
