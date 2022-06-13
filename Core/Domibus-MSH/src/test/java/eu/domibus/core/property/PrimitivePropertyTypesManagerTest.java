package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JMockit.class)
public class PrimitivePropertyTypesManagerTest {
    @Tested
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomibusPropertyProviderImpl domibusPropertyProvider;

    private String propertyName = "domibus.property.name";

    @Test()
    public void getIntInternal_number() {
        String customValue = "100";
        Integer res = primitivePropertyTypesManager.getIntegerInternal(propertyName, customValue);

        assertEquals(Integer.valueOf(customValue), res);
    }

    @Test()
    public void getIntInternal_null_and_not_number() {
        Integer defaultVal = 23;
        new Expectations(primitivePropertyTypesManager) {{
            primitivePropertyTypesManager.getDefaultIntegerValue(propertyName);
            returns(defaultVal, defaultVal);
        }};

        Integer res1 = primitivePropertyTypesManager.getIntegerInternal(propertyName, null);
        Integer res2 = primitivePropertyTypesManager.getIntegerInternal(propertyName, "aaaa");

        new Verifications() {{
            primitivePropertyTypesManager.getDefaultIntegerValue(propertyName);
            times = 2;
        }};
        assertEquals(defaultVal, res1);
        assertEquals(defaultVal, res2);
    }

    @Test()
    public void getLongInternal_number() {
        String customValue = "100";
        Long res = primitivePropertyTypesManager.getLongInternal(propertyName, customValue);

        assertEquals(Long.valueOf(customValue), res);
    }

    @Test()
    public void getLongInternal_null_and_not_number() {
        Long defaultVal = 23L;
        new Expectations(primitivePropertyTypesManager) {{
            primitivePropertyTypesManager.getDefaultLongValue(propertyName);
            returns(defaultVal, defaultVal);
        }};

        Long res1 = primitivePropertyTypesManager.getLongInternal(propertyName, null);
        Long res2 = primitivePropertyTypesManager.getLongInternal(propertyName, "aaaa");

        new Verifications() {{
            primitivePropertyTypesManager.getDefaultLongValue(propertyName);
            times = 2;
        }};
        assertEquals(defaultVal, res1);
        assertEquals(defaultVal, res2);
    }

    @Test()
    public void getBooleanInternal_ok() {
        String customValue = "true";
        Boolean res = primitivePropertyTypesManager.getBooleanInternal(propertyName, customValue);

        assertEquals(Boolean.valueOf(customValue), res);
    }

    @Test()
    public void getLongInternal_null_and_not_ok() {
        Boolean defaultVal = false;
        new Expectations(primitivePropertyTypesManager) {{
            primitivePropertyTypesManager.getDefaultBooleanValue(propertyName);
            returns(defaultVal, defaultVal);
        }};

        Boolean res1 = primitivePropertyTypesManager.getBooleanInternal(propertyName, null);
        Boolean res2 = primitivePropertyTypesManager.getBooleanInternal(propertyName, "aaaa");

        new Verifications() {{
            primitivePropertyTypesManager.getDefaultBooleanValue(propertyName);
            times = 2;
        }};
        assertEquals(defaultVal, res1);
        assertEquals(defaultVal, res2);
    }
}
