package eu.domibus.web.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;

import javax.validation.ConstraintValidator;
import java.lang.annotation.Annotation;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Interface for all the blacklist/whitelist validators
 */
public interface BlacklistValidator<A extends Annotation, T> extends ConstraintValidator<A, T> {
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
