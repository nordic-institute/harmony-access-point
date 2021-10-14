package eu.domibus.core.multitenancy;

import java.util.List;

public interface DynamicDomainManagementService {
    void checkAndHandleDomainsChanged();
    void addDomain(String dimainCode);
}
