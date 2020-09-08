package eu.domibus.web.rest.error;

import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.validation.ValidationException;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXCEPTIONS_REST_ENABLE;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * A service for packaging errors as REST Responses
 * It is called from the global error handler as well as from custom error handlers
 * It closes the connection in order to avoid a chrome-tomcat combination error
 */

@Service
public class ErrorHandlerService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorHandlerService.class);

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public ResponseEntity<ErrorRO> createResponse(Throwable ex) {
        return this.createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<ValidationResponseRO> createResponse(PModeValidationException ex, HttpStatus status) {
        LOG.error(ex.getMessage(), ex);

        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");

        ValidationResponseRO body = new ValidationResponseRO(ex.getMessage(), ex.getIssues());

        return new ResponseEntity(body, headers, status);
    }

    public ResponseEntity<ErrorRO> createResponse(Throwable ex, HttpStatus status) {
        LOG.error(ex.getMessage(), ex);

        //unwrap the domain task exception for the root error
        if (ex instanceof DomainTaskException) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            ex = rootCause == null ? ex : rootCause;
        }

        return createResponseEntity(ex.getMessage(), status);
    }

    public ResponseEntity<ErrorRO> createResponse(String message, HttpStatus status) {
        LOG.error(message);

        return createResponseEntity(message, status);
    }

    public void processBindingResultErrors(BindingResult bindingResult) throws ValidationException {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            String res = errors.stream().map(err -> err.getDefaultMessage())
                    .reduce(StringUtils.EMPTY, (accumulator, msg) -> accumulator + msg);
            throw new ValidationException(res);
        }
    }

    private ResponseEntity createResponseEntity(String message, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");

        boolean enabled = true;
        try {
            enabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE);
        } catch (Exception e) {
            LOG.warn("Error reading domibus.exceptions.rest.enable as boolean: [{}]", e.getMessage());
        }

        String errorMessage = enabled ? message : "A server error occurred";

        ErrorRO body = new ErrorRO(errorMessage);

        return new ResponseEntity(body, headers, status);
    }
}
