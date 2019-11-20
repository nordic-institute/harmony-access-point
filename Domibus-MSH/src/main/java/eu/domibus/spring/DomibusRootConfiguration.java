package eu.domibus.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration("domibusConfiguration")
@ImportResource({
        "classpath:META-INF/cxf/cxf.xml",
        "classpath:META-INF/cxf/cxf-extension-jaxws.xml",
        "classpath:META-INF/cxf/cxf-servlet.xml",
        "classpath*:META-INF/resources/WEB-INF/spring-context.xml"
})
public class DomibusRootConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusRootConfiguration.class);


}
