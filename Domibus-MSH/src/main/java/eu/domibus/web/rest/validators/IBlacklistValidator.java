package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.core.property.listeners.BlacklistChangeListener;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_USER_INPUT_BLACK_LIST;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_USER_INPUT_WHITE_LIST;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * * The base class that contains common code for all the blacklist/whitelist validators
 * * Basically, it checks that the value/values/object properties of the REST model do not contain any char from the blacklist and contain only chars from whitelist
 */
public interface IBlacklistValidator<A extends Annotation, T> extends ConstraintValidator<A, T> {
    void reset();

    void init();

    void validate(T value);

    String getErrorMessage();

    boolean isValid(T value, CustomWhiteListed customAnnotation);

    boolean isValid(T value);

    boolean isValidValue(String value);

    boolean isValidValue(String value, CustomWhiteListed customAnnotation);

    boolean isWhiteListValid(String value, CustomWhiteListed customAnnotation);

    boolean isBlackListValid(String value, CustomWhiteListed customAnnotation);
}
