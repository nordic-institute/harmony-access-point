package eu.domibus.web.rest.validators;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * The class that enforces the @SizeIfPresent constraint
 * The difference between this and the @Size is that it checks the size only if the value is not empty.(it accepts empty values)
 *
 * @author Ion Perpegel
 * since 4.1
 */
public class SizeIfPresentValidator implements ConstraintValidator<SizeIfPresent, String> {

    private SizeIfPresent constraintAnnotation;

    @Override
    public void initialize(SizeIfPresent constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // empty value accepted
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
