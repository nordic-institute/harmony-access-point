package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.messaging.XmlProcessingException;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Interface with some helper methods for pMode validation
 */
@Service
public interface PModeValidationHelper {

    <T> T getAttributeValue(Object object, String attribute, Class<T> type);

    PModeIssue createValidationIssue(String message, String name, String name2);

    PModeValidationException getPModeValidationException(XmlProcessingException e, String message);
}