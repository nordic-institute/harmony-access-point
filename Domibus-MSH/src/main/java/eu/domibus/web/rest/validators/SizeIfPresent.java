package eu.domibus.web.rest.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for custom Size validator: checks the size only if the value is not empty. ( it accepts empty values)
 *
 * @author Ion Perpegel
 * since 4.2
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = SizeIfPresentValidator.class)
@Documented
public @interface SizeIfPresent {

    String message() default "{javax.validation.constraints.Size.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 0;

    int max() default Integer.MAX_VALUE;
}
