package eu.domibus.web.rest.validators;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

public class RestQueryParamsValidationInterceptorTest {
    @Tested
    RestQueryParamsValidationInterceptor restQueryParamsValidationInterceptor;

    @Injectable
    ItemsBlacklistValidator blacklistValidator;

    @Test
    public void handleQueryParamsTestValid() {
        String[] arr1 = new String[]{"", "valid value", "also invalid value"};
        String[] arr2 = new String[]{"", "valid.value-2", "also invalid value-2"};

        new Expectations(restQueryParamsValidationInterceptor) {{
            blacklistValidator.isValid(arr1);
            result = true;
            blacklistValidator.isValid(arr2);
            result = true;
        }};

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", arr1);
        queryParams.put("param2", arr2);

        boolean actualValid = restQueryParamsValidationInterceptor.handleQueryParams(queryParams);

        Assert.assertEquals(true, actualValid);
    }

    @Test(expected = ValidationException.class)
    public void handleQueryParamsTestInValid() {
        String[] arr1 = new String[]{"", "valid value", "also invalid value"};
        String[] arr2 = new String[]{"", "invalid.value;2", "also invalid value%2"};

        new Expectations(restQueryParamsValidationInterceptor) {{
            blacklistValidator.isValid(arr1);
            result = true;
            blacklistValidator.isValid(arr2);
            result = false;
        }};

        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("param1", arr1);
        queryParams.put("param2", arr2);

        boolean actualValid = restQueryParamsValidationInterceptor.handleQueryParams(queryParams);
    }
}