package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import org.slf4j.Logger;
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
public class DomainResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DomainResource.class);

    private final DynamicDomainManagementService dynamicDomainManagementService;

    private final DomainService domainService;

    private final DomainDao domainDao;

    private final DomibusCoreMapper coreMapper;

    public DomainResource(DynamicDomainManagementService dynamicDomainManagementService, DomainService domainService,
                          DomainDao domainDao, DomibusCoreMapper coreMapper) {
        this.dynamicDomainManagementService = dynamicDomainManagementService;
        this.domainService = domainService;
        this.domainDao = domainDao;
        this.coreMapper = coreMapper;
    }

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
    public void addDomain(@RequestBody @Valid String domainCode) {
        dynamicDomainManagementService.addDomain(domainCode);
    }

    @DeleteMapping(value = "/{domainCode:.+}")
    public void removeDomain(@PathVariable(value = "domainCode") @Valid String domainCode) {
        dynamicDomainManagementService.removeDomain(domainCode);
    }
}
