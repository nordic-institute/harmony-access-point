package eu.domibus.ext.rest.error;

import eu.domibus.api.crypto.SameResourceCryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.CryptoExtException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.exceptions.PModeExtException;
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
        if (cause instanceof AuthenticationException) {
            return createResponseFromCoreException(cause, HttpStatus.UNAUTHORIZED);
        }

        if (cause instanceof CryptoExtException) {
            return createResponse(cause, HttpStatus.NOT_FOUND, true);
        }

        if (cause instanceof PModeExtException) {
            return createResponse(cause, HttpStatus.NOT_FOUND, true);
        }

        if (cause instanceof MessageNotFoundException) {
            return createResponse(cause, HttpStatus.NOT_FOUND, true);
        }

        if (cause instanceof SameResourceCryptoException) {
            return createResponse(cause, HttpStatus.OK, true);
        }

        if (cause instanceof DomibusCoreException) {
            if (((DomibusCoreException) cause).getError() == DomibusCoreErrorCode.DOM_009) {
                return createResponse(cause, HttpStatus.NOT_FOUND, true);
            }
            if (((DomibusCoreException) cause).getError() == DomibusCoreErrorCode.DOM_011) {
                return createResponse(cause, HttpStatus.CONFLICT, true);
            }
            if (((DomibusCoreException) cause).getError() == DomibusCoreErrorCode.DOM_002) {
                return createResponse(cause, HttpStatus.UNAUTHORIZED, true);
            }
            return createResponseFromCoreException(cause, HttpStatus.BAD_REQUEST);
        }

        if (cause instanceof DomibusCertificateException) {
            return createResponse(cause, HttpStatus.BAD_REQUEST, true);
        }

        //other exceptions wrapped by interceptors
        return createResponse(cause);
    }

    public ResponseEntity<ErrorDTO> handleExtException(AccessDeniedException accessDeniedException) {
        LOG.error("Access denied due to incorrect role:", accessDeniedException);
        return createResponse(accessDeniedException.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<ErrorDTO> handleExtException(AuthenticationException authenticationException) {
        LOG.error("Access denied due to incorrect role:", authenticationException);
        return createResponse(authenticationException.getMessage(), HttpStatus.UNAUTHORIZED);
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
        String errorMessage = getErrorMessage(ex, showErrorDetails);
        logException(ex, status, errorMessage);

        HttpHeaders headers = new HttpHeaders();
        ErrorDTO body = new ErrorDTO(errorMessage);
        return new ResponseEntity<>(body, headers, status);
    }

    private void logException(Throwable ex, HttpStatus status, String errorMessage) {
        if (status == HttpStatus.OK) {
            LOG.info(errorMessage);
            return;
        }
        LOG.error(errorMessage, ex);
    }

    private String getErrorMessage(Throwable ex, boolean showErrorDetails) {
        String errorMessage = "A server error occurred";
        if (showErrorDetails) {
            errorMessage = ex.getMessage();
            Throwable cause = extractCause(ex);
            if (cause != null && cause != ex) {
                errorMessage += ":" + cause.getMessage();
            }
        }
        return errorMessage;
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
        if (e.getCause() != null)
            return e.getCause();
        return e;
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
