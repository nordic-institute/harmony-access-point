package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.property.ConfigurationPropertyResourceHelper;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomibusPropertyRO;
import eu.domibus.web.rest.ro.DomibusPropertyTypeRO;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import eu.domibus.web.rest.ro.PropertyResponseRO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
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
    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationPropertyResource.class);

    @Autowired
    private ConfigurationPropertyResourceHelper configurationPropertyResourceHelper;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @GetMapping
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request) {
        PropertyResponseRO response = new PropertyResponseRO();

        List<DomibusProperty> items = configurationPropertyResourceHelper.getAllWritableProperties(request.getName(), request.isShowDomain());
        response.setCount(items.size());
        items = items.stream()
                .skip((long) request.getPage() * request.getPageSize())
                .limit(request.getPageSize())
                .collect(Collectors.toList());

        List<DomibusPropertyRO> convertedItems = domainConverter.convert(items, DomibusPropertyRO.class);

        response.setItems(convertedItems);

        return response;
    }

    @PutMapping(path = "/{propertyName:.+}")
    @SkipWhiteListed
    public void setProperty(@PathVariable String propertyName, @RequestParam(required = false, defaultValue = "true") boolean isDomain,
                            @RequestBody String propertyValue) {
        // sanitize empty body sent by various clients
        propertyValue = StringUtils.trimToEmpty(propertyValue);

        configurationPropertyResourceHelper.setPropertyValue(propertyName, isDomain, propertyValue);
    }

    /**
     * Exports to CSV
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid PropertyFilterRequestRO request) {
        List<DomibusProperty> items = configurationPropertyResourceHelper.getAllWritableProperties(request.getName(), request.isShowDomain());
        getCsvService().validateMaxRows(items.size());

        List<DomibusPropertyRO> convertedItems = domainConverter.convert(items, DomibusPropertyRO.class);

        return exportToCSV(convertedItems, DomibusPropertyRO.class, "domibusProperties");
    }

    /**
     * Retrieves the domibus property types (along with their regular expression) as a list,
     * To be used in client validation
     *
     * @return a list of property types
     */
    @RequestMapping(value = "metadata/types", method = RequestMethod.GET)
    public List<DomibusPropertyTypeRO> getDomibusPropertyMetadataTypes() {
        LOG.debug("Getting domibus property metadata types.");

        DomibusPropertyMetadata.Type[] types = DomibusPropertyMetadata.Type.values();
        List<DomibusPropertyTypeRO> res = domainCoreConverter.convert(Arrays.asList(types), DomibusPropertyTypeRO.class);
        return res;
    }

    /**
     * Returns the property metadata and the current value for a property
     * @param propertyName the name of the property
     * @return object containing both metadata and value
     */
    @GetMapping(path = "/{propertyName:.+}")
    public DomibusPropertyRO getProperty(@Valid @PathVariable String propertyName) {
        DomibusProperty prop = configurationPropertyResourceHelper.getProperty(propertyName);
        DomibusPropertyRO convertedProp = domainConverter.convert(prop, DomibusPropertyRO.class);
        return convertedProp;
    }
}
