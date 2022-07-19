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
     * @param domain - the domain for which the schema is checked
     * @return validity of the domain's db schema
     */
    boolean isDatabaseSchemaForDomainValid(Domain domain);

    /**
     * Create SQL command for changing the schema
     * @param databaseSchema - schema name
     * @return sql for schema change
     */
    String getSchemaChangeSQL(String databaseSchema);
}
