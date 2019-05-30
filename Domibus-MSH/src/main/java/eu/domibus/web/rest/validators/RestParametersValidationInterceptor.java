package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@ControllerAdvice(annotations = RestController.class)
public class RestParametersValidationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(RestParametersValidationInterceptor.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equals("POST")) {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
            throw new Exception("Blacllist character detected");
        }
        return true;
    }
}