package eu.domibus.ext.rest.error;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Helper methods for handling exceptions
 *
 * @author Catalin Enache
 * @since 4.2
 */
@Service
public class ExtExceptionHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ExtExceptionHelper.class);

    protected static final HttpStatus HTTP_STATUS_INVALID_REQUEST = HttpStatus.NOT_ACCEPTABLE;

    /**
     * Generic method to thread Ext exceptions
     * It unpacks Core exceptions
     *
     * @param extException Exception
     * @return ResponseEntity<ErrorDTO>
     */
    public ResponseEntity<ErrorDTO> handleExtException(DomibusServiceExtException extException) {
        Throwable cause = extractCause(extException);

        //Domibus core exceptions
        if (cause instanceof PModeValidationException) {
            return createResponseFromPModeValidationException((PModeValidationException) cause);
        }
        if (cause instanceof MessageNotFoundException) {
            return createResponse(cause, HttpStatus.NOT_FOUND, true);
        }

        if (cause instanceof DomibusCoreException) {
            if (((DomibusCoreException) cause).getError() == DomibusCoreErrorCode.DOM_009) {
                return createResponse(cause, HttpStatus.NOT_FOUND, true);
            }
            return createResponseFromCoreException(cause, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //other exceptions wrapped by interceptors
        return createResponse(cause);
    }

    public ResponseEntity<ErrorDTO> handleExtException(AccessDeniedException accessDeniedException) {
        LOG.error("Access denied due to incorrect role:", accessDeniedException);
        return createResponse(accessDeniedException.getMessage(), HttpStatus.UNAUTHORIZED);
    }


    protected ResponseEntity<ErrorDTO> createResponseFromCoreException(Throwable ex, HttpStatus httpStatus) {
        Throwable cause = extractCause(ex);
        return createResponse(cause, httpStatus, true);
    }

    protected ResponseEntity<ErrorDTO> createResponseFromPModeValidationException(PModeValidationException ex) {
        String errorMessage = getPModeValidationMessage(ex);
        return createResponse(errorMessage, HTTP_STATUS_INVALID_REQUEST);
    }

    protected ResponseEntity<ErrorDTO> createResponse(Throwable ex, HttpStatus status, boolean showErrorDetails) {
        String errorMessage = showErrorDetails ? ex.getMessage() : "A server error occurred";
        LOG.error(errorMessage, ex);

        HttpHeaders headers = new HttpHeaders();
        ErrorDTO body = new ErrorDTO(errorMessage);
        return new ResponseEntity<>(body, headers, status);
    }

    public ResponseEntity<ErrorDTO> createResponse(String errorMessage, HttpStatus status) {
        LOG.error(errorMessage);

        HttpHeaders headers = new HttpHeaders();
        ErrorDTO body = new ErrorDTO(errorMessage);
        return new ResponseEntity<>(body, headers, status);
    }

    protected ResponseEntity<ErrorDTO> createResponse(Throwable ex) {
        return createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, false);
    }

    /**
     * Return in a unique String both error message and validation issues
     *
     * @return error message String
     */
    public String getPModeValidationMessage(PModeValidationException e) {
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(e.getMessage())) {
            strBuilder.append(e.getMessage());
        }
        if (e.getIssues() != null) {
            strBuilder.append(". Validation issues: ").append(e.getIssues().stream().map(ValidationIssue::getMessage).collect(Collectors.joining(", ")));

        }
        return strBuilder.toString();
    }

    private Throwable extractCause(Throwable e) {
        //first level of cause exception
        return (e.getCause() == null ? e : e.getCause());
    }

    /**
     * Uses the enum name of the {@link DomibusCoreErrorCode} to identify the DomibusExtErrorCode.
     * Assumption is that the error codes for Core and Ext will be maintained as a 1-1 match.
     * If no match found, generic error code will be returned.
     */
    public DomibusErrorCode identifyExtErrorCodeFromCoreErrorCode(DomibusCoreErrorCode coreErrorCode) {
        try {
            return DomibusErrorCode.valueOf(coreErrorCode.name());
        } catch (IllegalArgumentException e) {
            LOG.debug("DomibusCoreErrorCode:[{}] does not match with any existing DomibusErrorCode. Mapping to generic error DOM_001.", coreErrorCode.name());
            return DomibusErrorCode.DOM_001;
        }
    }
}
