package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(JMockit.class)
public class DomibusPropertyManagerDelegateTest {

    @Injectable
    private DomibusPropertyManager domibusPropertyManager;

    @Injectable
    protected DomainExtConverter domainConverter;

    @Tested
    DomibusPropertyManagerDelegate domibusPropertyManagerDelegate;

    String domainCode = "domain1";
    String propertyName = "property1";
    String propertyValue = "propertyValue1";

    @Test
    public void getKnownProperties() {
        domibusPropertyManagerDelegate.getKnownProperties();

        new Verifications() {{
            Map<String, DomibusPropertyMetadata> props3 = domibusPropertyManager.getKnownProperties();
            domainConverter.convert(props3, DomibusPropertyMetadataDTO.class);
        }};
    }

    @Test
    public void getKnownPropertyValue() {
        new Expectations() {{
            domibusPropertyManager.getKnownPropertyValue(propertyName);
            result = propertyValue;
        }};

        String actual = domibusPropertyManagerDelegate.getKnownPropertyValue(propertyName);

        new Verifications() {{
            domibusPropertyManager.getKnownPropertyValue(propertyName);
        }};

        Assert.assertEquals(propertyValue, actual);
    }

    @Test
    public void setKnownPropertyValue() {
        domibusPropertyManagerDelegate.setKnownPropertyValue(domainCode, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyManager.setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
        }};
    }

    @Test
    public void testSetKnownPropertyValue() {
        domibusPropertyManagerDelegate.setKnownPropertyValue(propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyManager.setKnownPropertyValue(propertyName, propertyValue);
        }};
    }

    @Test
    public void hasKnownProperty() {
        new Expectations() {{
            domibusPropertyManager.hasKnownProperty(propertyName);
            result = true;
        }};

        boolean actual = domibusPropertyManagerDelegate.hasKnownProperty(propertyName);

        new Verifications() {{
            domibusPropertyManager.hasKnownProperty(propertyName);
        }};

        Assert.assertEquals(true, actual);
    }
}