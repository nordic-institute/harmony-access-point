package eu.domibus.api.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for Domibus Spring Web Contexts configurations
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomibusWebContext {
}