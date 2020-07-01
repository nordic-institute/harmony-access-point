package eu.domibus.web.rest.validators;

import eu.domibus.core.rest.validators.DomibusPropertyBlacklistValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * since 4.1
 */
@Target({METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = DomibusPropertyBlacklistValidator.class)
@Documented
public @interface DomibusPropertyWhiteListed {

    static String MESSAGE = "Forbidden character detected in property. ";

    String message() default MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
