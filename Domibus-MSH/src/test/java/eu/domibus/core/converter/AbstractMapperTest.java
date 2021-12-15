package eu.domibus.core.converter;

import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public abstract class AbstractMapperTest {
    @Configuration
    @ComponentScan(basePackages = "eu.domibus.core.converter", basePackageClasses = {EventMapper.class})
    @ImportResource({
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {
        @Bean
        public EArchiveBatchUtils archiveBatchUtils() {
            return new EArchiveBatchUtils();
        }
    }

    @Autowired
    protected ObjectService objectService;
}
