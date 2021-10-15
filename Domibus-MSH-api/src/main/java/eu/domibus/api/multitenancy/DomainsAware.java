package eu.domibus.api.multitenancy;

public interface DomainsAware {
    void onDomainAdded(final Domain domain);

    void onDomainRemoved(final Domain domain);
}
