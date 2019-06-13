package eu.domibus.web.rest;

import eu.domibus.core.property.PropertyService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import eu.domibus.web.rest.ro.PropertyRO;
import eu.domibus.web.rest.ro.PropertyResponseRO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/rest/configuration")
@Validated
public class PropertyResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PropertyResource.class);

    @Autowired
    private PropertyService propertyService;


    @GetMapping(path = "/properties")
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request) {
        PropertyResponseRO response = new PropertyResponseRO();
        List<PropertyRO> items = propertyService.getProperties(request.getName());
        response.setCount(items.size());
        items = items.stream()
                .skip(request.getPage() * request.getPageSize())
                .limit(request.getPageSize())
                .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }

    @GetMapping(path = "/superProperties")
    public PropertyResponseRO getProperties(@Valid PropertyFilterRequestRO request, String domain) {
        // TODO : set domain
        return this.getProperties(request);
    }
}
