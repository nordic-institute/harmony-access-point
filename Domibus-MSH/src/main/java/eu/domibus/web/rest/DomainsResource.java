package eu.domibus.web.rest;

import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    protected DomibusCoreMapper coreMapper;

    /**
     * Retrieve all configured domains in multi-tenancy mode
     *
     * @return a list of domains
     */
    @GetMapping(value = "")
    public List<DomainRO> getDomains() {
        LOG.debug("Getting active domains.");
        return coreMapper.domainListToDomainROList(domainService.getDomains());
    }

    @PostMapping(value = "")
    public void addDomain(@RequestBody @Valid String domainCode) throws RequestValidationException {
//        Domain domain = coreMapper.domainROToDomain(domainRO);
        dynamicDomainManagementService.addDomain(domainCode);
    }


}
