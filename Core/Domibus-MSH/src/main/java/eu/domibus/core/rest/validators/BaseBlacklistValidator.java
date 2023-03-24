package eu.domibus.core.rest.validators;

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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            if (StringUtils.isNotEmpty(propValue)) {
                whitelist = propValue;
            }
        }
        if (blacklist == null) {
            String propValue = domibusPropertyProvider.getProperty(BLACKLIST_PROPERTY);
            LOG.debug("Read the blacklist property: [{}]", propValue);
            if (StringUtils.isNotEmpty(propValue)) {
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
        validate(value, null);
    }

    public void validate(T value, String additionalWhitelist) {
        // create a "custom whitelist" instance to be able to include additional whitelisted characters
        // while validating against the default blacklist/whitelist configuration
        CustomWhiteListed customWhitelist = createCustomWhitelist(additionalWhitelist);
        if (!isValid(value, customWhitelist)) {
            LOG.debug("Value [{}] is not valid; Throwing exception.", value);
            throw new ValidationException(getErrorMessage());
        }
    }

    protected CustomWhiteListed createCustomWhitelist(CharSequence whitelistedCharacters) {
        if (StringUtils.isEmpty(whitelistedCharacters)) {
            return null;
        }
        return new CustomWhiteListed() {
            @Override
            public String permitted() {
                return whitelistedCharacters.toString();
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return CustomWhiteListed.class;
            }
        };
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
        LOG.trace("Validated value [{}] and the outcome is [{}]", value, res);
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
        if (StringUtils.isEmpty(value)) {
            LOG.trace("Value is empty, exiting");
            return true;
        }

        if (value.matches(whitelist)) {
            LOG.trace("Value [{}] matches the whitelist", value);
            return true;
        }

        final String additionalWhitelistCharacters = customAnnotation != null ? customAnnotation.permitted() : null;
        List<Character> forbiddenChars = findNonMatchingCharacters(value, Pattern.compile(whitelist), additionalWhitelistCharacters);
        if (CollectionUtils.isEmpty(forbiddenChars)) {
            LOG.trace("Value [{}] does not match the global whitelist, but matches the additional permitted list: [{}]", value, additionalWhitelistCharacters);
            return true;
        }

        LOG.debug("Forbidden chars: [{}]", forbiddenChars);
        return false;
    }

    protected List<Character> findNonMatchingCharacters(String inputValue, Pattern whitelistPattern, String whitelistCharacters) {
        final List<Character> inputCharacterList = inputValue
                .chars().mapToObj(valueChar -> (char) valueChar)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(inputCharacterList)) {
            return null;
        }

        final List<Character> forbiddenChars = findNonMatchingCharacters(inputCharacterList, whitelistPattern);
        if (CollectionUtils.isEmpty(forbiddenChars)) {
            return null;
        }

        return findNonMatchingCharacters(forbiddenChars, whitelistCharacters);
    }

    protected List<Character> findNonMatchingCharacters(List<Character> inputCharacterList, Pattern whitelistPattern) {
        return inputCharacterList.stream()
                .filter(valueChar -> !whitelistPattern.matcher(valueChar.toString()).matches())
                .distinct()
                .collect(Collectors.toList());
    }

    protected List<Character> findNonMatchingCharacters(List<Character> inputCharacterList, String whitelistCharacters) {
        if (StringUtils.isEmpty(whitelistCharacters)) {
            return inputCharacterList;
        }
        return inputCharacterList.stream()
                .filter(valueChar -> !whitelistCharacters.contains(valueChar.toString()))
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isBlackListValid(String value, CustomWhiteListed customAnnotation) {
        LOG.trace("Validating value [{}] in blacklist", value);
        if (blacklist == null) {
            LOG.trace("Blacklist is empty, exiting");
            return true;
        }
        if (StringUtils.isEmpty(value)) {
            LOG.trace("Value is empty, exiting");
            return true;
        }

        boolean res;
        String additionalPermitted = customAnnotation != null ? customAnnotation.permitted() : null;
        if (StringUtils.isNotEmpty(additionalPermitted)) {
            res = value.chars()
                    .mapToObj(valueChar -> (char) valueChar)
                    .noneMatch(valueChar -> blacklist.contains(valueChar)
                            && !additionalPermitted.contains(valueChar.toString()));
        } else {
            res = value.chars()
                    .mapToObj(valueChar -> (char) valueChar)
                    .noneMatch(valueChar -> blacklist.contains(valueChar));
        }
        LOG.trace("Validated value [{}] for blacklist and the outcome is [{}]", value, res);
        return res;
    }

}
