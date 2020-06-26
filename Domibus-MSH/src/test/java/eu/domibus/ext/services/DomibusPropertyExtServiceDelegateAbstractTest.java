package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(JMockit.class)
public class DomibusPropertyExtServiceDelegateAbstractTest extends TestCase {

    @Tested
    protected DomibusPropertyExtServiceDelegateAbstract domibusPropertyExtServiceDelegateAbstract;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    protected DomainExtService domainExtService;

    @Test
    public void getKnownPropertyValue_global(@Mocked String propertyName,
                                             @Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                             @Mocked DomibusPropertyMetadataDTO propMeta) {
        String propValue = "propValue";

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
            domibusPropertyExtService.getProperty(propertyName);
            result = propValue;
        }};

        String result = domibusPropertyExtServiceDelegateAbstract.getKnownPropertyValue(propertyName);

        Assert.assertEquals(propValue, result);
    }

    @Test
    public void getKnownPropertyValue_local(@Mocked String propertyName,
                                            @Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                            @Mocked DomibusPropertyMetadataDTO propMeta) {
        String propValue = "propValue";

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            domibusPropertyExtServiceDelegateAbstract.onGetLocalPropertyValue(propertyName, propMeta);
            result = propValue;
        }};

        String result = domibusPropertyExtServiceDelegateAbstract.getKnownPropertyValue(propertyName);

        Assert.assertEquals(propValue, result);

        new Verifications() {{
//            domibusPropertyExtService.getProperty(propertyName);
//            times = 0;
        }};
    }

    @Test
    public void getKnownIntegerPropertyValue_global(@Mocked String propertyName,
                                             @Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                             @Mocked DomibusPropertyMetadataDTO propMeta) {
        Integer propValue = 1;

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
            domibusPropertyExtService.getIntegerProperty(propertyName);
            result = propValue;
        }};

        Integer result = domibusPropertyExtServiceDelegateAbstract.getKnownIntegerPropertyValue(propertyName);

        Assert.assertEquals(propValue, result);
    }

    @Test
    public void getKnownIntegerPropertyValue_local(@Mocked String propertyName,
                                            @Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                            @Mocked DomibusPropertyMetadataDTO propMeta) {
        Integer propValue = 1;

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            domibusPropertyExtServiceDelegateAbstract.onGetLocalIntegerPropertyValue(propertyName, propMeta);
            result = propValue;
        }};

        Integer result = domibusPropertyExtServiceDelegateAbstract.getKnownIntegerPropertyValue(propertyName);

        Assert.assertEquals(propValue, result);
    }

    @Test
    public void setKnownPropertyValue_global(@Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                             @Mocked DomibusPropertyMetadataDTO propMeta,
                                             @Mocked DomainDTO domain) {
        String propertyValue = "propValue";
        String domainCode = "domainCode";
        String propertyName = "propertyName";
        boolean broadcast = true;

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
            domainExtService.getDomain(domainCode);
            result = domain;
        }};

        domibusPropertyExtServiceDelegateAbstract.setKnownPropertyValue(domainCode, propertyName, propertyValue, broadcast);

        new Verifications() {{
            domainExtService.getDomain(domainCode);
            domibusPropertyExtService.setDomainProperty(domain, propertyName, propertyValue);
        }};
    }

    @Test
    public void setKnownPropertyValue_local(@Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                            @Mocked DomibusPropertyMetadataDTO propMeta) {
        String propertyValue = "propValue";
        String domainCode = "domainCode";
        String propertyName = "propertyName";
        boolean broadcast = true;

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
        }};

        domibusPropertyExtServiceDelegateAbstract.setKnownPropertyValue(domainCode, propertyName, propertyValue, broadcast);

        new Verifications() {{
            domibusPropertyExtServiceDelegateAbstract.onSetLocalPropertyValue(domainCode, propertyName, propertyValue, broadcast);
            domainExtService.getDomain(domainCode);
            times = 0;
            domibusPropertyExtService.setDomainProperty((DomainDTO) any, propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test
    public void setKnownPropertyValue2_global(@Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                              @Mocked DomibusPropertyMetadataDTO propMeta) {
        String propertyValue = "propValue";
        String propertyName = "propertyName";

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
        }};

        domibusPropertyExtServiceDelegateAbstract.setKnownPropertyValue(propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyExtService.setProperty(propertyName, propertyValue);
        }};
    }

    @Test
    public void setKnownPropertyValue2_local(@Mocked Map<String, DomibusPropertyMetadataDTO> props,
                                              @Mocked DomibusPropertyMetadataDTO propMeta) {
        String propertyValue = "propValue";
        String propertyName = "propertyName";

        new Expectations(domibusPropertyExtServiceDelegateAbstract) {{
            domibusPropertyExtServiceDelegateAbstract.hasKnownProperty(propertyName);
            result = true;
            domibusPropertyExtServiceDelegateAbstract.getKnownProperties();
            result = props;
            props.get(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
        }};

        domibusPropertyExtServiceDelegateAbstract.setKnownPropertyValue(propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyExtServiceDelegateAbstract.onSetLocalPropertyValue(propertyName, propertyValue);
            domibusPropertyExtService.setProperty(propertyName, propertyValue);
            times=0;
        }};
    }
}