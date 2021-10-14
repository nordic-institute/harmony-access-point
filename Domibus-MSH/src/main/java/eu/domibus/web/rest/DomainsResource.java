package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Rest for managing MT domains
 */
@RestController
@RequestMapping(value = "/rest/domains")
public class DomainsResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomainsResource.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusCoreMapper coreMapper;
    
    /**
     * Retrieve all configured domains in multi-tenancy mode
     *
     * @return a list of domains
     */
    @RequestMapping(value = "domains", method = RequestMethod.GET)
    public List<DomainRO> getDomains() {
        LOG.debug("Getting domains");
        return coreMapper.domainListToDomainROList(domainService.getDomains());
    }


}
