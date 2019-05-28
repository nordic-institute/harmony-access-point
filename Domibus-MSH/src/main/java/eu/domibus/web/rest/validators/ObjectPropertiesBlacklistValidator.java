package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class ObjectPropertiesBlacklistValidator extends BaseBlacklistValidator<PropsNotBlacklisted, Object> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ObjectPropertiesBlacklistValidator.class);
    private String message = PropsNotBlacklisted.MESSAGE;

    @Override
    protected String getErrorMessage() {
        return message;
    }

    @Override
    public boolean isValid(Object obj) {
        List<Field> stringFields = Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(field -> field.getType() == String.class).collect(Collectors.toList());
        stringFields.forEach(field -> field.setAccessible(true));
        return stringFields.stream().allMatch(field -> isFieldValid(obj, field));
    }

    private String getValueSafely(Object obj, Field field) {
        try {
            return (String) field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    protected boolean isFieldValid(Object obj, Field field) {
        String val = getValueSafely(obj, field);
        boolean isValid = super.isStringValid(val);
        if (!isValid) {
            message = String.format(PropsNotBlacklisted.MESSAGE, field.getName());
        }
        return isValid;
    }
}
