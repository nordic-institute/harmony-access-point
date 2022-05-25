package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.ValidationResponseRO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Implementation of helper methods for pMode validation
 */
@Service
public class PModeValidationHelperImpl implements PModeValidationHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeValidationHelperImpl.class);

    @Override
    public <T> T getAttributeValue(Object object, String attribute, Class<T> type) {
        if (object == null) {
            return null;
        }
        Class clazz = object.getClass();
        try {
            Field field = clazz.getDeclaredField(attribute);
            field.setAccessible(true);
            Object val = field.get(object);
            return type.cast(val);
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException | ClassCastException e) {
            return null;
        }
    }

    @Override
    public ValidationIssue createValidationIssue(String message, String name, String name2) {
        String result;
        if (StringUtils.isEmpty(name)) {
            result = String.format(message.replaceFirst("\\[%s] ", ""), name2);
        } else {
            result = String.format(message, name, name2);
        }
        return new ValidationIssue(result, ValidationIssue.Level.ERROR);
    }

    @Override
    public PModeValidationException getPModeValidationException(XmlProcessingException e, String message) {
        if (CollectionUtils.isEmpty(e.getErrors())) {
            message += ExceptionUtils.getRootCauseMessage(e);
        }
        List<ValidationIssue> errors = e.getErrors().stream().map(err -> new ValidationIssue(err, ValidationIssue.Level.ERROR)).collect(Collectors.toList());
        return new PModeValidationException(message, errors);
    }

    @Override
    public ValidationResponseRO getValidationResponse(List<ValidationIssue> pmodeUpdateIssues, String message) {
        if (CollectionUtils.isNotEmpty(pmodeUpdateIssues)) {
            message += " Some issues were detected:";
        }

        return new ValidationResponseRO(message, pmodeUpdateIssues);
    }
}