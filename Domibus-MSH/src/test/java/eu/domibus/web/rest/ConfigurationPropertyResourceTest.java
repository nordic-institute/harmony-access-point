package eu.domibus.web.rest;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.property.ConfigurationPropertyResourceHelper;
import eu.domibus.web.rest.ro.DomibusPropertyRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class ConfigurationPropertyResourceTest {

    @Tested
    ConfigurationPropertyResource configurationPropertyResource;

    @Injectable
    private ConfigurationPropertyResourceHelper configurationPropertyResourceHelper;

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
            configurationPropertyResourceHelper.getProperty("propertyName");
            result = prop;
            domainConverter.convert(prop, DomibusPropertyRO.class);
            result = convertedProp;
        }};

        DomibusPropertyRO res = configurationPropertyResource.getProperty("propertyName");

        Assert.assertEquals(convertedProp, res);
    }
}