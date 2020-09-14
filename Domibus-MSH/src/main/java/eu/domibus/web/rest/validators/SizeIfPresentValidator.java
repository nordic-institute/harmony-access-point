package eu.domibus.web.rest.validators;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SizeIfPresentValidator implements ConstraintValidator<SizeIfPresent, String> {

    private SizeIfPresent constraintAnnotation;

    @Override
    public void initialize(SizeIfPresent constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        if (value.length() < constraintAnnotation.min()) {
            return false;
        }
        if (value.length() > constraintAnnotation.max()) {
            return false;
        }
        return true;
    }
}
