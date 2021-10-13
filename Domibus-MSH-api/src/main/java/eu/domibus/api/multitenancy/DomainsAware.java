package eu.domibus.api.multitenancy;

public interface DomainsAware {
    void domainAdded(final Domain domain);

    void domainRemoved(final Domain domain);
}
