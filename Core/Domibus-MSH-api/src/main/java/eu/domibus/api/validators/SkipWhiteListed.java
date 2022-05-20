package eu.domibus.api.validators;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used for disabling the blacklist validation for the property it decorates
 *
 * @author Ion Perpegel
 * since 4.1
 */
@Target({FIELD, PARAMETER, TYPE, METHOD})
@Retention(RUNTIME)
public @interface SkipWhiteListed {
}
