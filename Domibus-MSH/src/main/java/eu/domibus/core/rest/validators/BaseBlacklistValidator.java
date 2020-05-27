package eu.domibus.core.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_BLACK_LIST;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_USER_INPUT_WHITE_LIST;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * The base class that contains common code for all the blacklist/whitelist validators
 * Basically, it checks that the value/values/object properties of the REST model do not contain any char from the blacklist and contain only chars from whitelist
 */
public abstract class BaseBlacklistValidator<A extends Annotation, T> implements BlacklistValidator<A, T> {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(BaseBlacklistValidator.class);

    public static final String WHITELIST_PROPERTY = DOMIBUS_USER_INPUT_WHITE_LIST;
    public static final String BLACKLIST_PROPERTY = DOMIBUS_USER_INPUT_BLACK_LIST;

    protected String whitelist = null;
    protected Set<Character> blacklist = null;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    public void reset() {
        blacklist = null;
        whitelist = null;
        init();
    }

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

    public abstract String getErrorMessage();

    public abstract boolean isValid(T value, CustomWhiteListed customAnnotation);

    public boolean isValid(T value) {
        return isValid(value, (CustomWhiteListed) null);
    }

    public boolean isValidValue(String value) {
        return isValidValue(value, null);
    }

    public boolean isValidValue(String value, CustomWhiteListed customAnnotation) {
        boolean res = isWhiteListValid(value, customAnnotation) && isBlackListValid(value, customAnnotation);
        LOG.debug("Validated value [{}] and the outcome is [{}]", value, res);
        return res;
    }

    /**
     * @param value            the string value to be validated
     * @param customAnnotation optional custom annotation for permitting some more characters beside the general/common set defined in domibus properties
     *                         it is used for some properties, like endpoint, that need to allow some characters that otherwise are not permitted
     * @return if the value contain only characters that are defined in the whitelist domibus property and custom annotation, if specified
     */
    public boolean isWhiteListValid(String value, CustomWhiteListed customAnnotation) {
        LOG.trace("Validating value [{}] in whitelist", value);
        if (whitelist == null) {
            LOG.trace("Whitelist is empty, exiting");
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            LOG.trace("Value is empty, exiting");
            return true;
        }

        boolean valid = value.matches(whitelist);
        if (!valid && customAnnotation != null && StringUtils.isNotEmpty(customAnnotation.permitted())) {
            Optional<Character> forbiddenChar = value.chars().mapToObj(c -> (char) c).map(c -> c.toString())
                    .filter(el -> !el.matches(whitelist) && !customAnnotation.permitted().contains(el))
                    .map(s -> s.charAt(0))
                    .findFirst();
            valid = !forbiddenChar.isPresent();
            if (!valid) {
                LOG.debug("Forbidden char: [{}]", forbiddenChar.get());
            }
        }
        LOG.trace("Validated value [{}] for whitelist and the outcome is [{}]", value, valid);
        return valid;
    }

    public boolean isBlackListValid(String value, CustomWhiteListed customAnnotation) {
        LOG.trace("Validating value [{}] in blacklist", value);
        if (blacklist == null) {
            LOG.trace("Blacklist is empty, exiting");
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            LOG.trace("Value is empty, exiting");
            return true;
        }

        boolean res;
        if (customAnnotation != null && StringUtils.isNotEmpty(customAnnotation.permitted())) {
            res = value.chars().mapToObj(c -> (char) c).noneMatch(el -> blacklist.contains(el)
                    && !customAnnotation.permitted().contains(el.toString()));
        } else {
            res = value.chars().mapToObj(c -> (char) c).noneMatch(el -> blacklist.contains(el));
        }
        LOG.trace("Validated value [{}] for blacklist and the outcome is [{}]", value, res);
        return res;
    }

}
