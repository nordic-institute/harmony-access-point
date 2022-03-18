package eu.domibus.core.multitenancy;

public interface DynamicDomainManagementService {
    void addDomain(String domainCode);

    void removeDomain(String domainCode);
}
