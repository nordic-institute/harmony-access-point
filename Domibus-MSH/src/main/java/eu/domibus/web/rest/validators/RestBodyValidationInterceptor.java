package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.SkipWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.annotation.PostConstruct;
import javax.validation.ValidationException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author Ion Perpegel
 * @since 4.1
 * A Spring interceptor that ensures that the request body of a REST call does not contain blacklisted chars in any of its String properties
 */
@ControllerAdvice(basePackages = "eu.domibus.web.rest")
public class RestBodyValidationInterceptor extends RequestBodyAdviceAdapter {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(RestBodyValidationInterceptor.class);

    @PostConstruct
    public void init() {
        blacklistValidator.init();
    }

    @Autowired
    ObjectBlacklistValidator blacklistValidator;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        if (shouldSkipValidation(parameter.getMethod())) {
            LOG.debug("Skipping validation for method [{}]", parameter.getMethod());
            return body;
        }

        return handleRequestBody(body);
    }

    protected Object handleRequestBody(Object body) {
        LOG.debug("Validate body:[{}]", body);
        try {
            blacklistValidator.validate(body);
            LOG.debug("Body:[{}] is valid", body);
            return body;
        } catch (ValidationException ex) {
            LOG.debug("Body:[{}] is invalid: [{}]", body, ex);
            throw ex;
        } catch (Exception ex) {
            LOG.debug("Unexpected exception caught [{}] when validating body: [{}]. Request will be processed downhill.", ex, body);
            return body;
        }
    }

    private boolean shouldSkipValidation(Method method) {
        SkipWhiteListed skipAnnot = method.getAnnotation(SkipWhiteListed.class);
        if (skipAnnot != null) {
            return true;
        }
        if (ArrayUtils.isNotEmpty(method.getParameters())) {
            boolean skip = Arrays.stream(method.getParameters()).anyMatch(param -> param.getAnnotation(SkipWhiteListed.class) != null);
            if (skip) {
                return true;
            }
        }
        return false;
    }
}