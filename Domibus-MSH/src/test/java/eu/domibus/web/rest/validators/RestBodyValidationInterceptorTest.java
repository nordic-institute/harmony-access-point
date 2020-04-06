package eu.domibus.web.rest.validators;

import eu.domibus.core.rest.validators.ObjectBlacklistValidator;
import eu.domibus.web.rest.ro.MessageFilterRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.MethodParameter;

import javax.validation.ValidationException;
import java.lang.reflect.Type;

public class RestBodyValidationInterceptorTest {
    @Tested
    RestBodyValidationInterceptor restBodyValidationInterceptor;

    @Injectable
    ObjectBlacklistValidator blacklistValidator;

    @Test
    public void handleQueryParamsTestValid() {
        MessageFilterRO ro = new MessageFilterRO();
        ro.setPersisted(false);
        ro.setEntityId(1);
        ro.setBackendName("jms");

        new Expectations(restBodyValidationInterceptor) {{
            blacklistValidator.validate(ro);
        }};

        Object actualBody = restBodyValidationInterceptor.handleRequestBody(ro);

        Assert.assertEquals(ro, actualBody);
    }

    @Test(expected = ValidationException.class)
    public void handleQueryParamsTestInvalid() {
        MessageFilterRO ro = new MessageFilterRO();
        ro.setPersisted(false);
        ro.setEntityId(1);
        ro.setBackendName("jms;");

        new Expectations(restBodyValidationInterceptor) {{
            blacklistValidator.validate(ro);
            result = new ValidationException("Blacklist character detected");
        }};

        restBodyValidationInterceptor.handleRequestBody(ro);

    }

    @Test
    public void handleSupports(@Mocked MethodParameter methodParameter, @Mocked Type type) {

        boolean actual = restBodyValidationInterceptor.supports(methodParameter, type, null);

        Assert.assertEquals(true, actual);
    }
}