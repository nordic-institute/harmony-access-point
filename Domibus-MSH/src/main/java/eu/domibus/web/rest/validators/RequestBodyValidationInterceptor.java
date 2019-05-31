package eu.domibus.web.rest.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.annotation.PostConstruct;
import javax.validation.ValidationException;
import java.lang.reflect.Type;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@ControllerAdvice(annotations = RestController.class)
public class RequestBodyValidationInterceptor extends RequestBodyAdviceAdapter {

    @PostConstruct
    public void init() {
        blacklistValidator.init();
    }

    @Autowired
    DeepObjectBlacklistValidator blacklistValidator;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            blacklistValidator.validate(body);
            return body;
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            return body;
        }
    }
}