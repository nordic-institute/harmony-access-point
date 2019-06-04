package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The base, abstract class for custom validators that check that the value does not contain any char from the blacklist and contain only chars from whitelist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public abstract class BaseBlacklistValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseBlacklistValidator.class);

    protected String whitelist = null;
    protected Set<Character> blacklist = null;

    public static final String WHITELIST_PROPERTY = "domibus.userInput.whiteList";
    public static final String BLACKLIST_PROPERTY = "domibus.userInput.blackList";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public void init() {
        if (whitelist == null) {
            String propValue = domibusPropertyProvider.getProperty(WHITELIST_PROPERTY);
            LOG.debug("Read the whitelist property: [{}]", propValue);
            if (!Strings.isNullOrEmpty(propValue)) {
                whitelist = propValue;
            }
        }
        if (blacklist == null) {
            String propValue = domibusPropertyProvider.getProperty(BLACKLIST_PROPERTY);
            LOG.debug("Read the blacklist property: [{}]", propValue);
            if (!Strings.isNullOrEmpty(propValue)) {
                blacklist = new HashSet<>(Arrays.asList(ArrayUtils.toObject(propValue.toCharArray())));
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
        if (CollectionUtils.isEmpty(blacklist) && StringUtils.isEmpty(whitelist)) {
            LOG.debug("Exit validation as blacklist and whitelist are both empty");
            return true;
        }

        boolean isValid = isValid(value);
        if (!isValid) {
            String errorMessage = getErrorMessage();
            //disable existing violation message
            context.disableDefaultConstraintViolation();
            //build new violation message and add it
            context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();

            LOG.debug("Value [{}] is not valid; Preparing the error message: [{}]", value, errorMessage);
        }
        return isValid;
    }

    public void validate(T value) {
        if (!isValid(value)) {
            LOG.debug("Value [{}] is not valid; Throwing exception.", value);
            throw new ValidationException(getErrorMessage());
        }
    }

    protected abstract String getErrorMessage();

    public abstract boolean isValid(T value);

    protected boolean isValidValue(Collection<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            LOG.debug("Collection is empty, exiting");
            return true;
        }
        boolean res = values.stream().allMatch(el -> isValidValue(el));
        LOG.debug("Validated values: [{}] and the outcome is [{}]", values, res);
        return res;
    }

    protected boolean isValidValue(String[] values) {
        boolean res = isValidValue(Arrays.asList(values));
        LOG.debug("Validated values: [{}] and the outcome is [{}]", values, res);
        return res;
    }

    protected boolean isValidValue(String value) {
        boolean res = isWhiteListValid(value) && isBlackListValid(value);
        LOG.debug("Validated value [{}] and the outcome is [{}]", value, res);
        return res;
    }

    protected boolean isWhiteListValid(String value) {
        LOG.debug("Validating value [{}] in whitelist", value);
        if (whitelist == null) {
            LOG.debug("Whitelist is empty, exiting");
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            LOG.debug("Value is empty, exiting");
            return true;
        }

        boolean res = value.matches(whitelist);
        LOG.debug("Validated value [{}] for whitelist and the outcome is [{}]", value, res);
        return res;
    }

    protected boolean isBlackListValid(String value) {
        LOG.debug("Validating value [{}] in blacklist", value);
        if (blacklist == null) {
            LOG.debug("Blacklist is empty, exiting");
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            LOG.debug("Value is empty, exiting");
            return true;
        }

        boolean res = !value.chars().mapToObj(c -> (char) c).anyMatch(el -> blacklist.contains(el));
        LOG.debug("Validated value [{}] for blacklist and the outcome is [{}]", value, res);
        return res;
    }

}
