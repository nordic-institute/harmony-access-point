package eu.domibus.ext.rest.error;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ControllerAdvice for handling exceptions in EXT REST Api
 *
 * @author Catalin Enache
 * @since 4.2
 */
@ControllerAdvice("eu.domibus.ext.rest")
@RequestMapping(produces = "application/vnd.error+json")
public class ExtExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ExtExceptionHandlerAdvice.class);

    protected static final HttpStatus HTTP_STATUS_INVALID_REQUEST = HttpStatus.NOT_ACCEPTABLE;

    @ExceptionHandler(PModeExtException.class)
    public ResponseEntity<ErrorDTO> handlePModeExtServiceException(PModeExtException e) {
        return handlePModeExtException(e);
    }

    @ExceptionHandler(PartyExtServiceException.class)
    public ResponseEntity<ErrorDTO> handlePartyExtServiceException(PartyExtServiceException e) {
        return handleExtException(e);
    }

    @ExceptionHandler(MessageAcknowledgeExtException.class)
    public ResponseEntity<ErrorDTO> handleMessageAcknowledgeExtException(MessageAcknowledgeExtException e) {
        return handleExtException(e);
    }

    @ExceptionHandler(DomibusMonitoringExtException.class)
    public ResponseEntity<ErrorDTO> handleDomibusMonitoringExtException(DomibusMonitoringExtException e) {
        return handleExtException(e);
    }

    @ExceptionHandler(MessageMonitorExtException.class)
    public ResponseEntity<ErrorDTO> handleMessageMonitorExtException(MessageMonitorExtException e) {
        return handleExtException(e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        return createResponse(e);
    }

    /**
     * Generic method to thread Ext exceptions
     * It unpacks Core exceptions
     *
     * @param ex Exception
     * @return ResponseEntity<ErrorDTO>
     */
    protected ResponseEntity<ErrorDTO> handleExtException(DomibusServiceExtException ex) {
        Throwable cause = (ex.getCause() == null ? ex : ex.getCause());

        //Domibus core exceptions
        if (cause instanceof DomibusCoreException) {
            return createResponseFromCoreException(cause, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //other exceptions wrapped by interceptors
        return createResponse(cause);
    }

    /**
     * Handles PModeExtException including validation messages
     *
     * @param ex PModeExtException
     * @return esponseEntity<ErrorDTO>
     */
    protected ResponseEntity<ErrorDTO> handlePModeExtException(PModeExtException ex) {
        String errorMessage = ex.getErrorMessage() ;
        HttpHeaders headers = new HttpHeaders();
        ErrorDTO body = new ErrorDTO(errorMessage);
        LOG.error(errorMessage, ex);
        return new ResponseEntity(body, headers, HTTP_STATUS_INVALID_REQUEST);
    }

    protected ResponseEntity<ErrorDTO> createResponse(Throwable ex, HttpStatus status, boolean showErrorDetails) {
        String errorMessage = showErrorDetails ? ex.getMessage() : "A server error occurred";
        HttpHeaders headers = new HttpHeaders();
        ErrorDTO body = new ErrorDTO(errorMessage);
        LOG.error(errorMessage, ex);
        return new ResponseEntity(body, headers, status);
    }

    protected ResponseEntity<ErrorDTO> createResponse(Throwable ex) {
        return createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, false);
    }

    protected ResponseEntity<ErrorDTO> createResponseFromCoreException(Throwable ex, HttpStatus httpStatus) {
        Throwable cause = (ex.getCause() == null ? ex : ex.getCause());
        return createResponse(cause, httpStatus, true);
    }

    /**
     * Handles the exception behavior for @Valid annotated input methods
     *
     * @param ex MethodArgumentNotValidException
     * @param headers HttpHeaders
     * @param status HttpStatus
     * @param request WebRequest
     * @return errors list
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        LOG.error(ex.getMessage(), ex);
        BindingResult result = ex.getBindingResult();
        List<String> errorsList = result
                .getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ResponseEntity(errorsList, HTTP_STATUS_INVALID_REQUEST);
    }

}
