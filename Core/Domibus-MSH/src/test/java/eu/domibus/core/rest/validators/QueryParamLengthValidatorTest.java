package eu.domibus.core.rest.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ValidationException;
import java.util.HashMap;

/**
 * @author François Gautier
 * @since 5.0
 */
public class QueryParamLengthValidatorTest {

    private QueryParamLengthValidator queryParamLengthValidator;
    private HashMap<String, String[]> queryParams;

    @Before
    public void setUp() throws Exception {
        queryParamLengthValidator = new QueryParamLengthValidator();
        queryParams = new HashMap<>();

    }

    @Test
    public void validateEmpty() {
        queryParams.put("empty", new String[]{});
        queryParamLengthValidator.validate(queryParams);
    }

    @Test
    public void validateBlank() {
        queryParams.put("empty", new String[]{""});
        queryParamLengthValidator.validate(queryParams);
    }

    @Test
    public void validateTooLong() {
        queryParams.put("empty", new String[]{"1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"});

        try {
            queryParamLengthValidator.validate(queryParams);
            Assert.fail();
        } catch (ValidationException e) {
            //ok
        }
    }
}