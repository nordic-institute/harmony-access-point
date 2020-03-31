package eu.domibus.core.spring;

import eu.domibus.web.rest.validators.RestQueryParamsValidationInterceptor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration("domibusConfiguration")
@ImportResource({
        "classpath:META-INF/cxf/cxf.xml",
        "classpath:META-INF/cxf/cxf-extension-jaxws.xml",
        "classpath:META-INF/cxf/cxf-servlet.xml",
        "classpath*:META-INF/resources/WEB-INF/spring-context.xml"
})
@ComponentScan(
        basePackages = "eu.domibus"
)
@EnableJms
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableCaching
public class DomibusRootConfiguration {

}
