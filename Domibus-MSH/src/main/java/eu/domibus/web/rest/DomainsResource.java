package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.security.DomibusUserDetails;
import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private final DynamicDomainManagementService dynamicDomainManagementService;

    private final DomainService domainService;

    private final DomainDao domainDao;

    private final DomibusCoreMapper coreMapper;

    private final AuthUtils authUtils;

    private final DomibusConfigurationService domibusConfigurationService;

    public DomainsResource(DynamicDomainManagementService dynamicDomainManagementService, DomainService domainService,
                           DomainDao domainDao, DomibusCoreMapper coreMapper, AuthUtils authUtils, DomibusConfigurationService domibusConfigurationService) {
        this.dynamicDomainManagementService = dynamicDomainManagementService;
        this.domainService = domainService;
        this.domainDao = domainDao;
        this.coreMapper = coreMapper;
        this.authUtils = authUtils;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    /**
     * Retrieve domains in multi-tenancy mode ( active or potential)
     *
     * @return a list of domains
     */
    @GetMapping(value = "")
    public List<DomainRO> getDomains(@Valid Boolean active) {
        List<Domain> domains = new ArrayList<>();
        if (active == null) {
            LOG.debug("Getting all domains");
            domains = domainDao.findAll();
        } else if (active) {
            LOG.debug("Getting active domains");
            UserDetails userDetails = authUtils.getUserDetails();
            if (userDetails instanceof DomibusUserDetails) {
                List<Domain> availableDomains = ((DomibusUserDetails) userDetails).getAvailableDomainCodes().stream()
                        .map(domainService::getDomain)
                        .collect(Collectors.toList());
                domains.addAll(availableDomains);
            }
        }
        return coreMapper.domainListToDomainROList(domains);
    }

    @PostMapping(value = "")
    public void addDomain(@RequestBody @Valid String domainCode){
        dynamicDomainManagementService.addDomain(domainCode);
    }
}
