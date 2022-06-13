package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.DynamicDomainManagementService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.security.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
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

    private final DomibusCoreMapper coreMapper;

    private final AuthenticationService authenticationService;

    private final UserDomainService userDomainService;

    private final DomainContextProvider domainContextProvider;

    public DomainResource(DynamicDomainManagementService dynamicDomainManagementService,
                          DomainService domainService, DomibusCoreMapper coreMapper,
                          AuthenticationService authenticationService, UserDomainService userDomainService, DomainContextProvider domainContextProvider) {
        this.dynamicDomainManagementService = dynamicDomainManagementService;
        this.domainService = domainService;
        this.coreMapper = coreMapper;
        this.authenticationService = authenticationService;
        this.userDomainService = userDomainService;
        this.domainContextProvider = domainContextProvider;
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
            domains = domainService.getAllDomains();
        } else if (active) {
            domains = domainService.getDomains();
        }
        return coreMapper.domainListToDomainROList(domains);
    }

    @PostMapping(value = "")
    public void addDomain(@RequestBody @Valid String domainCode) {
        dynamicDomainManagementService.addDomain(domainCode, true);
    }

    @DeleteMapping(value = "/{domainCode:.+}")
    public void removeDomain(@PathVariable(value = "domainCode") @Valid String domainCode) {
        DomibusUserDetails domibusUserDetails = authenticationService.getLoggedUser();
        String preferredDomainCode = userDomainService.getPreferredDomainForUser(domibusUserDetails.getUsername());

        if (StringUtils.equalsIgnoreCase(domainCode, preferredDomainCode)) {
            throw new ValidationException("Cannot disable the domain of the current user");
        }

        Domain currentDomain = domainContextProvider.getCurrentDomain();
        if (StringUtils.equalsIgnoreCase(domainCode, currentDomain.getCode())) {
            throw new ValidationException("Cannot disable the current domain of your session");
        }

        dynamicDomainManagementService.removeDomain(domainCode, true);
    }
}
