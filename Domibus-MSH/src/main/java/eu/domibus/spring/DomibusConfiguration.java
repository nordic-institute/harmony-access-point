package eu.domibus.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Configuration("domibusConfiguration")
@ImportResource({
        "classpath:META-INF/cxf/cxf.xml",
        "classpath:META-INF/cxf/cxf-extension-jaxws.xml",
        "classpath:META-INF/cxf/cxf-servlet.xml",
        "classpath*:META-INF/resources/WEB-INF/spring-context.xml"
})
public class DomibusConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusConfiguration.class);


}
