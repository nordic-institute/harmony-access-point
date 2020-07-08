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
 * Annotation for custom validator that checks that a domibus property value is valid
 *
 * @author Ion Perpegel
 * since 4.2
 */
@Target({METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = DomibusPropertyBlacklistValidator.class)
@Documented
public @interface DomibusPropertyWhiteListed {

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
