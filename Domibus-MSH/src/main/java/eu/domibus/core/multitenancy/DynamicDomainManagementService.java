package eu.domibus.core.multitenancy;

public interface DynamicDomainManagementService {
    void addDomain(String domainCode, boolean notifyClusterNodes);

    void removeDomain(String domainCode, boolean notifyClusterNodes);
}
