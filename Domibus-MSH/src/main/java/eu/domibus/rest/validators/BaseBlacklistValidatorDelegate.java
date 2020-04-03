package eu.domibus.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.web.rest.validators.IBlacklistValidator;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Standard delegate implementation for blacklist validators
 */
@Component
public class BaseBlacklistValidatorDelegate<A extends Annotation, T> implements IBlacklistValidator<A, T> {

    protected IBlacklistValidator<A, T> baseBlacklistValidator;

    public void setBaseBlacklistValidator(IBlacklistValidator<A, T> baseBlacklistValidator) {
        this.baseBlacklistValidator = baseBlacklistValidator;
    }

    @Override
    public void reset() {
        baseBlacklistValidator.reset();
    }

    @Override
    public void init() {
        baseBlacklistValidator.init();
    }

    @Override
    public void validate(T value) {
        baseBlacklistValidator.validate(value);
    }

    @Override
    public String getErrorMessage() {
        return baseBlacklistValidator.getErrorMessage();
    }

    @Override
    public boolean isValid(T value, CustomWhiteListed customAnnotation) {
        return baseBlacklistValidator.isValid(value, customAnnotation);
    }

    @Override
    public boolean isValid(T value) {
        return baseBlacklistValidator.isValid(value);
    }

    @Override
    public boolean isValidValue(String value) {
        return baseBlacklistValidator.isValidValue(value);
    }

    @Override
    public boolean isValidValue(String value, CustomWhiteListed customAnnotation) {
        return baseBlacklistValidator.isValidValue(value, customAnnotation);
    }

    @Override
    public boolean isWhiteListValid(String value, CustomWhiteListed customAnnotation) {
        return baseBlacklistValidator.isWhiteListValid(value, customAnnotation);
    }

    @Override
    public boolean isBlackListValid(String value, CustomWhiteListed customAnnotation) {
        return baseBlacklistValidator.isBlackListValid(value, customAnnotation);
    }

    @Override
    public boolean isValid(T value, ConstraintValidatorContext constraintValidatorContext) {
        return baseBlacklistValidator.isValid(value, constraintValidatorContext);
    }
}
