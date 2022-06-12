package eu.domibus.api.multitenancy;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Interface implemented by beans that want to react to adding and removing of domains at runtime
 */
public interface DomainsAware {
    void onDomainAdded(final Domain domain);

    void onDomainRemoved(final Domain domain);
}
