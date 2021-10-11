package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;

public interface DomainsAware {
    void domainsChanged(final List<Domain> added, final List<Domain> removed);
}
