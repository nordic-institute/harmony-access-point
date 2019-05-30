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
import java.util.*;

/**
 * The base, abstract class for custom validators that check that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public abstract class BaseBlacklistValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {
    //private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseBlacklistValidator.class);

    protected Set<Character> whitelist = null;
    protected Set<Character> blacklist = null;

    public static final String WHITELIST_PROPERTY = "domibus.userInput.whiteList";
    public static final String BLACKLIST_PROPERTY = "domibus.userInput.blackList";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public void init() {
        if (whitelist == null) {
            String propValue = domibusPropertyProvider.getProperty(WHITELIST_PROPERTY);
            if (!Strings.isNullOrEmpty(propValue)) {
                whitelist = new HashSet<>(Arrays.asList(ArrayUtils.toObject(propValue.toCharArray())));
            }
        }
        if (blacklist == null) {
            String propValue = domibusPropertyProvider.getProperty(BLACKLIST_PROPERTY);
            if (!Strings.isNullOrEmpty(propValue)) {
                this.blacklist = new HashSet<>(Arrays.asList(ArrayUtils.toObject(propValue.toCharArray())));
            }
        }
    }

    @Override
    public void initialize(A attr) {
        init();
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (CollectionUtils.isEmpty(blacklist) && CollectionUtils.isEmpty(whitelist)) {
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

    protected boolean isValidValue(Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return true;
        }
        return values.stream().allMatch(el -> isValidValue(el));
    }

    protected boolean isValidValue(String[] values) {
        return isValidValue(Arrays.asList(values));
    }

    protected boolean isValidValue(String value) {
        return isWhiteListValid(value) && isBlackListValid(value);
    }

    protected boolean isWhiteListValid(String value) {
        if (whitelist == null) {
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        boolean res = value.chars().mapToObj(c -> (char) c).allMatch(el -> whitelist.contains(el));
        return res;
    }

    protected boolean isBlackListValid(String value) {
        if (blacklist == null) {
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        boolean res = !value.chars().mapToObj(c -> (char) c).anyMatch(el -> blacklist.contains(el));
        return res;
    }

}
