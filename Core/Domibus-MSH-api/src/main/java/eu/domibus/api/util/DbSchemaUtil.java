package eu.domibus.api.util;

import eu.domibus.api.multitenancy.Domain;

/**
 * Provides functionality for testing if a domain has a valid database schema
 *
 * @author Lucian FURCA
 * @since 5.1
 */
public interface DbSchemaUtil {

    String getDatabaseSchema(Domain domain);

    /**
     * This method is used to retrieve the schema change sql statement, conforming to the current database type
     *
     * @param databaseSchemaName the database schema name to be checked
     * @throws DomibusDatabaseNotSupportedException database type is not supported
     * @throws FaultyDatabaseSchemaNameException database schema name is invalid
     * @return the sql statement that changes the db schema
     */
    String getSchemaChangeSQL(String databaseSchemaName) throws DomibusDatabaseNotSupportedException, FaultyDatabaseSchemaNameException;

    /**
     * Checks if the database schema associated to the domain can be accessed
     * @param domain - the domain for which the schema is checked
     * @return validity of the domain's db schema
     */
    boolean isDatabaseSchemaForDomainValid(Domain domain);

    String getGeneralSchema();

    void removeCachedDatabaseSchema(Domain domain);

    /**
     * Checks the database schema name sanity(it may contain only alphanumeric and "_" characters)
     *
     * @param schemaName the database schema name to be checked
     * @return result of the db schema sanity check
     */
    boolean isDatabaseSchemaNameSane(final String schemaName);
}
