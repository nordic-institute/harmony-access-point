package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@ControllerAdvice(annotations = RestController.class)
public class RestParametersValidationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(RestParametersValidationInterceptor.class);

    @PostConstruct
    public void init() {
        blacklistValidator.init();
    }

    @Autowired
    ItemsBlacklistValidator blacklistValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getMethod().equals("GET")) {
            return true;
        }

        try {
            validate(request.getParameterMap(), response);
            return true;
        } catch (ValidationException ex) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            throw ex;
        }
    }

    private void validate(Map<String, String[]> parameterMap, HttpServletResponse response) {
        parameterMap.forEach((key, val) -> {
            if (!blacklistValidator.isValid(val)) {
                throw new ValidationException(String.format(PropsNotBlacklisted.MESSAGE, key));
            }
        });
    }
}