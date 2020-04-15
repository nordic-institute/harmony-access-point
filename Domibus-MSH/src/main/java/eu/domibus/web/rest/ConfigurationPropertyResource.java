package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.property.ConfigurationPropertyService;
import eu.domibus.web.rest.ro.DomibusPropertyRO;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import eu.domibus.web.rest.ro.PropertyResponseRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Resource responsible for getting the domibus properties that can be changed at runtime, getting and setting their values through REST Api
 */
@RestController
@RequestMapping(value = "/rest/configuration/properties")
@Validated
public class ConfigurationPropertyResource extends BaseResource {

    @Autowired
    private ConfigurationPropertyService configurationPropertyService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    @GetMapping
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request) {
        PropertyResponseRO response = new PropertyResponseRO();

        List<DomibusProperty> items = configurationPropertyService.getAllWritableProperties(request.getName(), request.isShowDomain());
        response.setCount(items.size());
        items = items.stream()
                .skip((long) request.getPage() * request.getPageSize())
                .limit(request.getPageSize())
                .collect(Collectors.toList());

        List<DomibusPropertyRO> convertedItems = domainConverter.convert(items, DomibusPropertyRO.class);

        response.setItems(convertedItems);

        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    @PutMapping(path = "/{propertyName:.+}")
    @SkipWhiteListed
    public void setProperty(@PathVariable String propertyName, @RequestParam(required = false, defaultValue = "true") boolean isDomain,
                            @RequestBody String propertyValue) {
        // sanitize empty body sent by various clients
        propertyValue = StringUtils.trimToEmpty(propertyValue);

        configurationPropertyService.setPropertyValue(propertyName, isDomain, propertyValue);
    }

    /**
     * Exports to CSV
     *
     * @param request
     * @return
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid PropertyFilterRequestRO request) {
        List<DomibusProperty> items = configurationPropertyService.getAllWritableProperties(request.getName(), request.isShowDomain());
        csvServiceImpl.validateMaxRows(items.size());

        List<DomibusPropertyRO> convertedItems = domainConverter.convert(items, DomibusPropertyRO.class);

        return exportToCSV(convertedItems, DomibusPropertyRO.class, "domibusProperties");
    }

}
