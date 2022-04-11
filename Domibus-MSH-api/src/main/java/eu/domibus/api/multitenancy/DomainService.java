package eu.domibus.api.multitenancy;

import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_GENERAL_SCHEMA;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainService {

    Domain DEFAULT_DOMAIN = new Domain("default", "Default");

    String DOMAINS_HOME = "domains";

    String GENERAL_SCHEMA_PROPERTY = DOMIBUS_DATABASE_GENERAL_SCHEMA;

    List<Domain> getDomains();

    List<Domain> getAllDomains();

    Domain getDomain(String code);

    Domain getDomainForScheduler(String schedulerName);

    String getDatabaseSchema(Domain domain);

    String getGeneralSchema();

    String getSchedulerName(Domain domain);

    void removeDomain(String domainCode);

    void addDomain(Domain domain);

    void refreshDomain(String domainCode);
}
