package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class FSMultiTenancyService {

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    private DomainExtService domainExtService;

    /**
     * This method returns true if {#domain} is configured.
     *
     *
     * @param domain Domain name
     * @return true if we are in Multi Tenancy configuration and domain is configured, or if we are in Single Tenancy
     * configuration and the domain is the DEFAULT_DOMAIN. Otherwise it throws FSSetUpException (if the domain was
     * not configured)
     */
    public boolean verifyDomainExists(String domain) {
        if (domainExtService.getDomain(domain) == null) {
            throw new FSSetUpException("Domain " + domain + " not configured in Domibus");
        }
        return true;
    }
}
