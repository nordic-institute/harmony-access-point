package eu.domibus.web.rest.validators;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to customize the general blacklist validation
 *
 * @author Ion Perpegel
 * since 4.1
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface CustomBlacklistValidation {
    //the characters of the property are permitted even if they appear in blacklist property or do not appear in white list property
    //they do no override the whole blacklist property but adds to it; it is like an exception to the general rule
    String permitted() default "";
}
