package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * default domain in single tenancy and current domain in multitenancy mode
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
