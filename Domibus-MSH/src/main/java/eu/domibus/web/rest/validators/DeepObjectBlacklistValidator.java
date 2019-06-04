package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Custom validator for classes that checks if all properties of type String and collections of String
 * do not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class DeepObjectBlacklistValidator extends BaseBlacklistValidator<DeepObjectNotBlacklisted, Object> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DeepObjectBlacklistValidator.class);
    private String message = DeepObjectNotBlacklisted.MESSAGE;

    @Override
    protected String getErrorMessage() {
        return message;
    }

    @Override
    public boolean isValid(Object obj) {
        try {
            doValidate(obj, "root");
            return true;
        } catch (ValidationException ex) {
            return false;
        }
    }

    protected void doValidate(Object obj, String path) {
        if (obj == null) {
            return;
        }
        if (obj instanceof String) {
            if (!isValidValue((String) obj)) {
                message = String.format(PropsNotBlacklisted.MESSAGE, path);
                throw new ValidationException(message);
            }
        } else if (obj instanceof Object[]) {
            doValidate(Arrays.asList((Object[]) obj), path);
        } else if (obj instanceof List<?>) {
            List<?> list = ((List<?>) obj);
            for (int i = 0; i < list.size(); i++) {
                doValidate(list.get(i), path + "[" + (i + 1) + "]");
            }
        } else if (obj instanceof Iterable<?>) {
            ((Iterable<?>) obj).forEach(el -> doValidate(el, path + "[one of the elements]"));
        } else if (obj instanceof Map<?, ?>) {
            ((Map<?, ?>) obj).forEach((key, val) -> doValidate(val, path + "[" + key + "]"));
        } else if (!isPrimitive(obj)) {
            ReflectionUtils.doWithFields(obj.getClass(),
                    field -> {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Object value = ReflectionUtils.getField(field, obj);
                        doValidate(value, path + "->" + field.getName());
                    },
                    field -> true
            );
        }
    }

    private boolean isPrimitive(Object obj) {
        return ClassUtils.isPrimitiveOrWrapper(obj.getClass())
                || isDate(obj);
    }

    private boolean isDate(Object obj) {
        Class<?> cls = obj.getClass();
        return cls.equals(Date.class) || cls.equals(LocalDate.class) || cls.equals(LocalDateTime.class);
    }
    //field.getAnnotationsByType(EscapeHTML.class).length > 0 &&
    //String.class.isAssignableFrom(field.getType())
}
