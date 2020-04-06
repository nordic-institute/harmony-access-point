package eu.domibus.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.web.rest.validators.BlacklistValidator;

import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Standard delegate implementation for blacklist validators, used for crossing the spring context boundary
 * derived classes are injected by Spring instead of the actual business classes and delegate implementation to them
 */
public abstract class BaseBlacklistValidatorDelegate<A extends Annotation, T> implements BlacklistValidator<A, T> {

    protected BlacklistValidator<A, T> delegated;

    public void setDelegated(BlacklistValidator<A, T> delegated) {
        this.delegated = delegated;
    }

    @Override
    public void reset() {
        delegated.reset();
    }

    @Override
    public void init() {
        delegated.init();
    }

    @Override
    public void validate(T value) {
        delegated.validate(value);
    }

    @Override
    public String getErrorMessage() {
        return delegated.getErrorMessage();
    }

    @Override
    public boolean isValid(T value, CustomWhiteListed customAnnotation) {
        return delegated.isValid(value, customAnnotation);
    }

    @Override
    public boolean isValid(T value) {
        return delegated.isValid(value);
    }

    @Override
    public boolean isValidValue(String value) {
        return delegated.isValidValue(value);
    }

    @Override
    public boolean isValidValue(String value, CustomWhiteListed customAnnotation) {
        return delegated.isValidValue(value, customAnnotation);
    }

    @Override
    public boolean isWhiteListValid(String value, CustomWhiteListed customAnnotation) {
        return delegated.isWhiteListValid(value, customAnnotation);
    }

    @Override
    public boolean isBlackListValid(String value, CustomWhiteListed customAnnotation) {
        return delegated.isBlackListValid(value, customAnnotation);
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
        return delegated.isValid(value, constraintValidatorContext);
    }
}
