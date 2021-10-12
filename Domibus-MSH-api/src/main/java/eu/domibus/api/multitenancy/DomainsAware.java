package eu.domibus.api.multitenancy;

import java.util.List;

public interface DomainsAware {
    void domainsChanged(final List<Domain> added, final List<Domain> removed);
}
