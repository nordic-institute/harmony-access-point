package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public abstract class BaseBlacklistValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    //private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseBlacklistValidator.class);
    protected Character[] blacklist = null;

    public static final String BLACKLIST_PROPERTY = "domibus.userInput.blackList";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext context1;

    public void init() {
        if (blacklist == null) {
            String blacklistValue = domibusPropertyProvider.getProperty(BLACKLIST_PROPERTY);
            if (!Strings.isNullOrEmpty(blacklistValue)) {
                this.blacklist = ArrayUtils.toObject(blacklistValue.toCharArray());
            }
        }
    }

    @Override
    public void initialize(A attr) {
        init();
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        if (ArrayUtils.isEmpty(blacklist)) {
            return true;
        }
        if (value == null) {
            return true;
        }

        boolean isValid = isValid(value);
        if (!isValid) {
            //disable existing violation message
            context.disableDefaultConstraintViolation();
            //build new violation message and add it
            context.buildConstraintViolationWithTemplate(getErrorMessage()).addConstraintViolation();
        }
        return isValid;
    }

    public void validate(T value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(getErrorMessage());
        }
    }

    protected abstract String getErrorMessage();

    public abstract boolean isValid(T value);

    protected boolean isValidValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        return !Arrays.stream(blacklist).anyMatch(el -> value.contains(el.toString()));
    }

    public boolean isValidValue(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        return list.stream().allMatch(el -> isValidValue(el));
    }

}
