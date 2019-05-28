package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public abstract class BaseBlacklistValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    //private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseBlacklistValidator.class);

    public static final String BLACKLIST_PROPERTY = "domibus.userInput.blackList";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    Character[] blacklist = null;

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
        return isValid(value);
    }

    public void validate(T value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(getErrorMessage());
        }
    }

    protected abstract String getErrorMessage();

    public boolean isValid(T value) {
        if (ArrayUtils.isEmpty(blacklist)) {
            return true;
        }
        if (value == null) {
            return true;
        }

        return false;
    }

}
