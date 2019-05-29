package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom validator for classes that checks if all properties of type String and List[String]
 *  do not contain any char from the blacklist
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
                .filter(field -> field.getType() == String.class || isStringList(field))
                .collect(Collectors.toList());
        stringFields.forEach(field -> field.setAccessible(true));
        return stringFields.stream().allMatch(field -> isFieldValid(obj, field));
    }

    private Object getValueSafely(Object obj, Field field) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private boolean isFieldValid(Object obj, Field field) {
        Object val = getValueSafely(obj, field);
        if (val == null) {
            return true;
        }
        boolean isValid;
        if (val instanceof String) {
            isValid = super.isValidValue((String) val);
        } else {
            isValid = super.isValidValue((List<String>) val);
        }
        if (!isValid) {
            message = String.format(PropsNotBlacklisted.MESSAGE, field.getName());
        }
        return isValid;
    }

    private boolean isStringList(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] args = pt.getActualTypeArguments();
            return args.length == 1 && args[0].getTypeName().equals(String.class.getTypeName());
        }
        return false;
    }
}
