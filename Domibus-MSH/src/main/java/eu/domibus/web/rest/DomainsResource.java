package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
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
    DynamicDomainManagementService dynamicDomainManagementService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    DomainDao domainDao;

    @Autowired
    protected DomibusCoreMapper coreMapper;

    /**
     * Retrieve domains in multi-tenancy mode ( active or potential)
     *
     * @return a list of domains
     */
    @GetMapping(value = "")
    public List<DomainRO> getDomains(@Valid Boolean active) {
        List<Domain> domains = Arrays.asList();
        if (active == null) {
            LOG.debug("Getting all domains.");
            domains = domainDao.findAll();
        } else if (active) {
            LOG.debug("Getting active domains.");
            domains = domainService.getDomains();
        }
        return coreMapper.domainListToDomainROList(domains);
    }

    @PostMapping(value = "")
    public void addDomain(@RequestBody @Valid String domainCode) throws RequestValidationException {
//        Domain domain = coreMapper.domainROToDomain(domainRO);
        dynamicDomainManagementService.addDomain(domainCode);
    }


}
