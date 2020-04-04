package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.rest.validators.ObjectPropertiesMapBlacklistValidatorDelegate;
import eu.domibus.web.rest.ro.JmsFilterRequestRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ObjectPropertiesMapBlacklistValidatorTest {
    @Tested
    ObjectPropertiesMapBlacklistValidator blacklistValidator;

    @Injectable
    ItemsBlacklistValidator listValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ObjectPropertiesMapBlacklistValidatorDelegate objectPropertiesMapBlacklistValidatorDelegate;

    @Test
    public void handleTestValid_NoClassInfo() {
        String[] arr1 = new String[]{"", "valid value",};
        String[] arr2 = new String[]{"", "valid.value2",};

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", arr1);
        queryParams.put("param2", arr2);

        new Expectations(listValidator) {{
            listValidator.isValid((String[]) any, (CustomWhiteListed) any);
            result = true;
        }};

        ObjectPropertiesMapBlacklistValidator.Parameter payload = new ObjectPropertiesMapBlacklistValidator.Parameter(queryParams, null, null);
        boolean actualValid = blacklistValidator.isValid(payload, (CustomWhiteListed) null);

        Assert.assertEquals(true, actualValid);
    }

    @Test
    public void handleTestValid_ClassInfo() {
        String[] arr1 = new String[]{"", "valid value",};
        String[] arr2 = new String[]{"", "valid.value",};

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", arr1);
        queryParams.put("param2", arr2);

        new Expectations(listValidator) {{
            listValidator.isValid((String[]) any, (CustomWhiteListed) any);
            result = true;
        }};

        ObjectPropertiesMapBlacklistValidator.Parameter payload =
                new ObjectPropertiesMapBlacklistValidator.Parameter(queryParams, JmsFilterRequestRO.class, null);
        boolean actualValid = blacklistValidator.isValid(payload, (CustomWhiteListed) null);

        Assert.assertEquals(true, actualValid);
    }

    @Test
    public void handleTestInvalid_ClassInfo() {
        String[] arr1 = new String[]{"", "valid value",};
        String[] arr2 = new String[]{"", "invalid.value;=",};

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", arr1);
        queryParams.put("param2", arr2);

        new Expectations(listValidator) {{
            listValidator.isValid((String[]) any, (CustomWhiteListed) any);
            result = false;
        }};

        ObjectPropertiesMapBlacklistValidator.Parameter payload =
                new ObjectPropertiesMapBlacklistValidator.Parameter(queryParams, JmsFilterRequestRO.class, null);
        boolean actualValid = blacklistValidator.isValid(payload, (CustomWhiteListed) null);

        Assert.assertEquals(false, actualValid);
    }
}