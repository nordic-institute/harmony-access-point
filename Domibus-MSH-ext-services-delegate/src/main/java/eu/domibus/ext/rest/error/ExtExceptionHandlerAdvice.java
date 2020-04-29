package eu.domibus.ext.rest.error;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(AuthenticationExtException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationExtException(AuthenticationExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @ExceptionHandler(DomibusServiceExtException.class)
    public ResponseEntity<ErrorDTO> handleDomibusServiceExtException(DomibusServiceExtException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        return extExceptionHelper.createResponse(e);
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
        return new ResponseEntity(errorsList, ExtExceptionHelper.HTTP_STATUS_INVALID_REQUEST);
    }
}
