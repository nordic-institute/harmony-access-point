package eu.domibus.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration("domibusWebConfiguration")
@ImportResource({
        "classpath:META-INF/resources/WEB-INF/mvc-dispatcher-servlet.xml"
})
public class DomibusWebConfiguration {
}
