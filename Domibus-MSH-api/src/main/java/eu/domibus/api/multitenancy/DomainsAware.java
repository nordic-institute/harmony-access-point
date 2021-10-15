package eu.domibus.api.multitenancy;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface DomainsAware {
    void onDomainAdded(final Domain domain);

    void onDomainRemoved(final Domain domain);
}
