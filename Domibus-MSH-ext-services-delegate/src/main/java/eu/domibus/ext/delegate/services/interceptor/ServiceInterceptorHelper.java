package eu.domibus.ext.delegate.services.interceptor;

import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.exceptions.PModeExtException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper/common methods for Service Interceptor
 *
 * @author Catalin Enache
 * @since 4.2
 */
@Service
public class ServiceInterceptorHelper {

    /**
     * Intercepts a {@code PModeValidationException} and set validation issues to {@code PModeExtException}
     *
     * @param coreException     of type PModeValidationException
     * @param pModeExtException of type PModeExtException
     */
    public void handlePModeValidationException(Exception coreException, PModeExtException pModeExtException) {
        if (pModeExtException != null && coreException instanceof PModeValidationException) {
            if (((PModeValidationException) coreException).getIssues() != null) {
                List<String> issues = ((PModeValidationException) coreException).getIssues().stream().map(ValidationIssue::getMessage).collect(Collectors.toList());
                pModeExtException.setValidationIssues(issues);
            }
            throw pModeExtException;
        }
    }
}
