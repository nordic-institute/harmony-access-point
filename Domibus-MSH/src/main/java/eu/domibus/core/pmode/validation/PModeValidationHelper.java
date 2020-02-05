package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>

 */
@Service
public class PModeValidationHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeValidationHelper.class);

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

    public void createIssue(List<PModeIssue> issues, String name, String message, String name2) {
        String result;
        if(StringUtils.isEmpty(name)) {
            result = String.format(message.replaceFirst("\\[%s] ", ""), name2);
        } else {
            result = String.format(message, name, name2);
        }
        issues.add(new PModeIssue(result, PModeIssue.Level.ERROR));
    }
}