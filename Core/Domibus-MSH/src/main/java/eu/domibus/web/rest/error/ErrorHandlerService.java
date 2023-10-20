package eu.domibus.web.rest.error;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.TestErrorsInfoRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.ValidationException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EXCEPTIONS_REST_ENABLE;
import static eu.domibus.web.rest.error.ErrorMessages.DEFAULT_MESSAGE_FOR_AUTHENTICATION_ERRORS;
import static eu.domibus.web.rest.error.ErrorMessages.DEFAULT_MESSAGE_FOR_GENERIC_ERRORS;

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

    final DomibusPropertyProvider domibusPropertyProvider;

    public ErrorHandlerService(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public ResponseEntity<ErrorRO> createResponse(Throwable ex) {
        return this.createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<TestErrorsInfoRO> createResponse(TestServiceException ex, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");

        TestErrorsInfoRO body = Optional.ofNullable(ex.getDetails()).orElse(new TestErrorsInfoRO(ex.getMessage()));
        return new ResponseEntity(body, headers, status);
    }

    public ResponseEntity<ValidationResponseRO> createResponse(PModeValidationException ex, HttpStatus status) {
        LOG.warn(ex.getMessage() + " : " + ex.getIssues().toString(), ex);

        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");

        ValidationResponseRO body = new ValidationResponseRO(ex.getMessage(), ex.getIssues());

        return new ResponseEntity(body, headers, status);
    }

    public ResponseEntity<ErrorRO> createResponse(Throwable ex, HttpStatus status) {
        logException(ex, status);

        //unwrap the domain task exception for the root error
        if (ex instanceof DomainTaskException || ex instanceof CryptoException) {
            Throwable rootCause = ExceptionUtils.getRootCause(ex);
            ex = rootCause == null ? ex : rootCause;
        }
        String errorMessage;
        if(AuthenticationException.class.isAssignableFrom(ex.getClass())) {
            errorMessage = DEFAULT_MESSAGE_FOR_AUTHENTICATION_ERRORS;
        } else {
            errorMessage = getErrorMessage(ex.getMessage());
        }

        return createResponseEntity(status, errorMessage);
    }

    private void logException(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.OK) {
            LOG.info(ex.getMessage());
            return;
        }
        LOG.error(ex.getMessage(), ex);
    }

    public ResponseEntity<ErrorRO> createResponse(String message, HttpStatus status) {
        LOG.error(message);

        return createResponseEntity(status, getErrorMessage(message));
    }

    public void processBindingResultErrors(BindingResult bindingResult) throws ValidationException {
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            String res = errors.stream().map(err -> err.getDefaultMessage())
                    .reduce(StringUtils.EMPTY, (accumulator, msg) -> accumulator + msg);
            throw new ValidationException(res);
        }
    }

    private ResponseEntity createResponseEntity(HttpStatus status, String errorMessage) {
        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");
        ErrorRO body = new ErrorRO(errorMessage);
        return new ResponseEntity(body, headers, status);
    }

    private String getErrorMessage(String message) {
        boolean enabled = true;
        try {
            enabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EXCEPTIONS_REST_ENABLE);
        } catch (Exception e) {
            LOG.warn("Error reading domibus.exceptions.rest.enable as boolean: [{}]", e.getMessage());
        }

        if (enabled) {
            return message;
        }
        return DEFAULT_MESSAGE_FOR_GENERIC_ERRORS;
    }

    public ResponseEntity<ErrorRO> createConstraintViolationResponse(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(el -> getLast(el.getPropertyPath()) + " " + el.getMessage())
                .reduce("There are validation errors: ", (accumulator, element) -> accumulator + element + "; ");
        return createResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ErrorRO> createHibernateExceptionResponse(HibernateException ex) {
        LOG.error("Hibernate error", ex);
        // hide precise errors (like SQL statements from the response) - see EDELIVERY-9027
        String genericHibernateExcMsg = "Persistence exception occurred";
        return createResponse(genericHibernateExcMsg, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected String getLast(Path propertyPath) {
        Iterator<Path.Node> it = propertyPath.iterator();
        while (true) {
            Path.Node node = it.next();
            if (!it.hasNext()) {
                return node.toString();
            }
        }
    }

}
