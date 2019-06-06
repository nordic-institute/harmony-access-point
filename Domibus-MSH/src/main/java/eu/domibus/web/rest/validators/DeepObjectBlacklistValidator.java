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
 * do not contain any char from the blacklist but only chars from the whitelist
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
        LOG.debug("Validating recursively the object properties [{}]", obj);
        try {
            doValidate(obj, "root");
            LOG.debug("All object properties [{}] are valid.", obj);
            return true;
        } catch (ValidationException ex) {
            LOG.debug("At least one of the object properties [{}] is not valid.", obj);
            return false;
        }
    }

    protected void doValidate(Object obj, String path) {
        if (obj == null) {
            LOG.debug("Object [{}] to validate is null, exiting.", path);
            return;
        }
        if (obj.getClass().getAnnotation(SkipBlacklistValidation.class) != null) {
            return;
        }
        if (obj instanceof String) {
            LOG.debug("Validating object String property [{}]:[{}]", path, obj);
            if (!isValidValue((String) obj)) {
                message = String.format(ObjectPropertiesNotBlacklisted.MESSAGE, path);
                throw new ValidationException(message);
            }
        } else if (obj instanceof Object[]) {
            LOG.debug("Validating object array property [{}]:[{}]", path, obj);
            doValidate(Arrays.asList((Object[]) obj), path);
        } else if (obj instanceof List<?>) {
            LOG.debug("Validating object List property [{}]:[{}]", path, obj);
            List<?> list = ((List<?>) obj);
            for (int i = 0; i < list.size(); i++) {
                doValidate(list.get(i), path + "[" + (i + 1) + "]");
            }
        } else if (obj instanceof Iterable<?>) {
            LOG.debug("Validating object Iterable property [{}]:[{}]", path, obj);
            ((Iterable<?>) obj).forEach(el -> doValidate(el, path + "[one of the elements]"));
        } else if (obj instanceof Map<?, ?>) {
            LOG.debug("Validating object Map property [{}]:[{}]", path, obj);
            ((Map<?, ?>) obj).forEach((key, val) -> doValidate(val, path + "[" + key + "]"));
        } else if (!isPrimitive(obj)) {
            LOG.debug("Validating all object non-primitive properties [{}]:[{}]", path, obj);
            ReflectionUtils.doWithFields(obj.getClass(),
                    field -> {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Object value = ReflectionUtils.getField(field, obj);
                        this.customAnnotation = field.getAnnotation(CustomBlacklistValidation.class);
                        doValidate(value, path + "->" + field.getName());
                    },
                    field -> (field.getAnnotation(SkipBlacklistValidation.class) == null)
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
}
