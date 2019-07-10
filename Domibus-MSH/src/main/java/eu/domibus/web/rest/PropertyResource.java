package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.Property;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.property.DomibusPropertyService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import eu.domibus.web.rest.ro.PropertyRO;
import eu.domibus.web.rest.ro.PropertyResponseRO;
import eu.domibus.web.rest.validators.SkipWhiteListed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * Resource responsible with getting the domibus properties that can be changed at runtime, getting and setting their values throught REST Api
 */
@RestController
@RequestMapping(value = "/rest/configuration")
@Validated
public class PropertyResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PropertyResource.class);

    @Autowired
    private DomibusPropertyService domibusPropertyService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    @GetMapping(path = "/properties")
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request) {
        PropertyResponseRO response = new PropertyResponseRO();
        List<Property> items = domibusPropertyService.getProperties(request.getName());
        response.setCount(items.size());
        items = items.stream()
                .skip(request.getPage() * request.getPageSize())
                .limit(request.getPageSize())
                .collect(Collectors.toList());
        response.setItems(domainConverter.convert(items, PropertyRO.class));
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_AP_ADMIN')")
    @GetMapping(path = "/superProperties")
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request, String domain) {
        if (StringUtils.isEmpty(domain)) {
            domainContextProvider.clearCurrentDomain();
        } else
            domainContextProvider.setCurrentDomain(domain);

        return this.getProperties(request);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    @PutMapping(path = "/properties/{propertyName:.+}")
    @SkipWhiteListed
    public void setProperty(@PathVariable String propertyName, @RequestBody String propertyValue) {
        domibusPropertyService.setPropertyValue(propertyName, propertyValue);
    }

    @PreAuthorize("hasAnyRole('ROLE_AP_ADMIN')")
    @PutMapping(path = "/superProperties/{propertyName:.+}")
    @SkipWhiteListed
    public void setProperty(@PathVariable String propertyName, @RequestBody String propertyValue, String domain) {
        if (StringUtils.isEmpty(domain)) {
            domainContextProvider.clearCurrentDomain();
        } else {
            domainContextProvider.setCurrentDomain(domain);
        }

        this.setProperty(propertyName, propertyValue);
    }
}
