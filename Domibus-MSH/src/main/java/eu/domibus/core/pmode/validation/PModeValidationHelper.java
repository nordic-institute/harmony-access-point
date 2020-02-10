package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Interface with some helper methods for pMode validation
 */
@Service
public interface PModeValidationHelper {

    <T> T getAttributeValue(Object object, String attribute, Class<T> type);

    ValidationIssue createValidationIssue(String message, String name, String name2);

    PModeValidationException getPModeValidationException(XmlProcessingException e, String message);

    ValidationResponseRO getValidationResponse(List<ValidationIssue> pmodeUpdateIssues, String message);
}