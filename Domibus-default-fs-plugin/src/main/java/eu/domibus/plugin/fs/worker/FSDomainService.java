package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tiago Miguel
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class FSDomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSDomainService.class);

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    protected DomainExtService domainExtService;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected DomainContextExtService domainContextExtService;


    /**
     * Verifies if the provided domain exists.
     * For multitenancy mode it checks if the FS Plugin domain is configured in Domibus core.
     * For non multitenancy mode it always returns true.
     *
     * @param domain FS Plugin domain
     * @return true if we are in Multi Tenancy configuration and domain is configured. Otherwise it returns true.
     */
    public boolean verifyDomainExists(String domain) {
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            LOG.debug("Checking if the domain [{}] is configured in Domibus core", domain);

            if (domainExtService.getDomain(domain) == null) {
                throw new FSSetUpException("Domain " + domain + " not configured in Domibus");
            }
            LOG.debug("Domain [{}] is configured in Domibus core", domain);
            return true;
        }
        LOG.trace("Provided domain [{}] is configured in non multitenancy mode", domain);
        return true;
    }

    /**
     * Returns all domains that need to be processed by various operations (sending, purging etc)
     * In multi-tenancy, this means only the current domain.
     * In single-tenancy, default domain
     *
     * @return a list of domain codes
     */
    public List<String> getDomainsToProcess() {

        return Arrays.asList(domainContextExtService.getCurrentDomain().getCode());

    }

    /**
     * Resolves FS Plugin domain to Domibus core domain
     *
     * @param fsPluginDomain The FS Plugin domain to resolved
     * @return the Domibus core domain
     */
    public DomainDTO fsDomainToDomibusDomain(String fsPluginDomain) {
        if (!domibusConfigurationExtService.isMultiTenantAware()) {
            fsPluginDomain = FSSendMessagesService.DEFAULT_DOMAIN;
            LOG.debug("Domibus is running in single tenancy mode, using the Domibus default domain");
        }

        final DomainDTO domainDTO = domainExtService.getDomain(fsPluginDomain);
        LOG.debug("Resolving FSPlugin domain [{}] to Domibus domain [{}]", fsPluginDomain, domainDTO);
        return domainDTO;
    }

    /**
     * Gets the FS Plugin domain from the Domibus core:
     * default domain in singletenancy and current domain in multitenancy mode
     *
     * @return the FS Plugin domain
     */
    public String getFSPluginDomain() {
        LOG.trace("Getting FSPluginDomain");

        // get domain info
        String domain = domainContextExtService.getCurrentDomain().getCode();
        LOG.trace("FSPluginDomain is [{}]", domain);

        return domain;
    }

}
