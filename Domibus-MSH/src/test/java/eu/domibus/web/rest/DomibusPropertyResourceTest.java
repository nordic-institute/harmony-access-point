package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.property.DomibusPropertyResourceHelper;
import eu.domibus.core.property.DomibusPropertiesFilter;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.DomibusPropertyRO;
import eu.domibus.web.rest.ro.PropertyFilterRequestRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DomibusPropertyResourceTest {

    @Tested
    DomibusPropertyResource domibusPropertyResource;

    @Injectable
    private DomibusPropertyResourceHelper domibusPropertyResourceHelper;

    @Injectable
    private ErrorHandlerService errorHandlerService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    protected DomainCoreConverter domainCoreConverter;

    @Injectable
    CsvServiceImpl csvService;

    @Test
    public void getProperty(@Mocked DomibusProperty prop, @Mocked DomibusPropertyRO convertedProp) {
        new Expectations() {{
            domibusPropertyResourceHelper.getProperty("propertyName");
            result = prop;
            domainConverter.convert(prop, DomibusPropertyRO.class);
            result = convertedProp;
        }};

        DomibusPropertyRO res = domibusPropertyResource.getProperty("propertyName");

        Assert.assertEquals(convertedProp, res);
    }

    @Test
    public void getCsv(@Injectable DomibusProperty prop, @Injectable DomibusPropertyRO convertedProp,
                       @Injectable DomibusPropertiesFilter filter,
                       @Injectable PropertyFilterRequestRO request) {

        List<DomibusPropertyRO> convertedItems = new ArrayList<>();
        convertedItems.add(convertedProp);
        List<DomibusProperty> items = new ArrayList<>();
        items.add(prop);

        new Expectations() {{
            domainConverter.convert(request, DomibusPropertiesFilter.class);
            result = filter;
            domibusPropertyResourceHelper.getAllProperties(filter);
            result = items;
            domainConverter.convert(items, DomibusPropertyRO.class);
            result = convertedItems;
        }};

        ResponseEntity<String> res = domibusPropertyResource.getCsv(request);

        Assert.assertEquals(HttpStatus.OK, res.getStatusCode());
    }
}