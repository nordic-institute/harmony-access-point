package eu.domibus.web.rest.error;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.web.rest.ro.ErrorRO;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.0
 * <p>
 * A global error handler for REST interfaces;
 * the last resort if the error is not caught in the controller where it originated
 */

@ControllerAdvice("eu.domibus.web.rest")
@RequestMapping(produces = "application/vnd.error+json")
public class GlobalExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @ExceptionHandler({DomainTaskException.class})
    public ResponseEntity<ErrorRO> handleDomainException(DomainTaskException ex) {
        return handleWrappedException(ex);
    }

    @ExceptionHandler({RollbackException.class})
    public ResponseEntity<ErrorRO> handleRollbackException(RollbackException ex) {
        return handleWrappedException(ex);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorRO> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RequestValidationException.class})
    public ResponseEntity<ErrorRO> handleRequestValidationException(RequestValidationException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorRO> handleValidationException(ValidationException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorRO> handleRuntimeException(RuntimeException ex) {
        return errorHandlerService.createResponse(ex);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorRO> handleException(Exception ex) {
        return errorHandlerService.createResponse(ex);
    }

    @ExceptionHandler({PModeException.class})
    public ResponseEntity<ErrorRO> handlePModeException(PModeException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PModeValidationException.class})
    public ResponseEntity<ValidationResponseRO> handleValidationException(PModeValidationException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CsvException.class})
    public ResponseEntity<ErrorRO> handleCsvException(CsvException ex) {
        return errorHandlerService.createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorRO> handleConstraintViolationException(ConstraintViolationException ex) {
        return errorHandlerService.createConstraintViolationResponse(ex);
    }

    private ResponseEntity<ErrorRO> handleWrappedException(Exception ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex) == null ? ex : ExceptionUtils.getRootCause(ex);

        return errorHandlerService.createResponse(rootCause);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> list = ex.getBindingResult()
                .getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ResponseEntity(list, HttpStatus.BAD_REQUEST);
    }
}
