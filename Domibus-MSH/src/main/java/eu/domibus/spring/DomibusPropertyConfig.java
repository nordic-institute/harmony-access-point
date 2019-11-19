package eu.domibus.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * <p>Configuration providing common property sources to the {@code Environment} in order
 * to ensure the placeholders get correctly replaced inside bean definition property values
 * and {@code @Value} fields. It is intended to be used as a replacement for individual
 * {@code PropertySource} annotations placed on classes implementing {@code Condition}.</p>
 * <p>
 * The following property sources are being checked in the reverse order (first
 * the file://../domibus.properties, then the classpath://../domibus.properties, etc
 *
 * @author Sebastian-Ion TINCU
 * @author Cosmin Baciu
 * @see org.springframework.core.env.Environment
 * @see org.springframework.context.annotation.Condition
 * @since 4.1
 */
//@Configuration
//@PropertySources({
//        @PropertySource("classpath:config/application.properties"),
//        @PropertySource("classpath:config/domibus-default.properties"),
//        @PropertySource(value = "classpath:config/domibus.properties", ignoreResourceNotFound = true),
//        @PropertySource("file:///${domibus.config.location}/domibus.properties"),
//        @PropertySource(value = "file:///${domibus.config.location}/*-domibus.properties", ignoreResourceNotFound = true),
//        @PropertySource(value = "classpath*:config/*-plugin-default.properties", ignoreResourceNotFound = true),
//        @PropertySource(value = "file:///${domibus.config.location}/plugins/config/*-plugin.properties", ignoreResourceNotFound = true),
//        @PropertySource(value = "file:///${domibus.config.location}/extensions/config/*-extension.properties", ignoreResourceNotFound = true),
//})
public class DomibusPropertyConfig {

}