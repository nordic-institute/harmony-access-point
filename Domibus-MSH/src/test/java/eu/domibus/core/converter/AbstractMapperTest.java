package eu.domibus.core.converter;

import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.mapper.EventMapperImpl_;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import eu.domibus.core.message.UserMessageLogDao;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import mockit.Injectable;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    @ImportResource({
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {
        @Injectable
        private UserMessageLogDao userMessageLogDao;

        @Bean
        public EventMapper eventMapper() {
            return new EventMapperImpl_();
        }

        @Bean
        public EArchiveBatchUtils eArchiveBatchUtils() {
            return new EArchiveBatchUtils(userMessageLogDao);
        }

        @Bean
        public AuditLogCoreMapper auditLogCoreMapper() {
            return new AuditLogCoreMapperImpl();
        }

        @Bean
        public AuthCoreMapper authCoreMapper() {
            return new AuthCoreMapperImpl();
        }

        @Bean
        public AlertCoreMapper alertCoreMapper() {
            return new AlertCoreMapperImpl();
        }

        @Bean
        public BackendFilterCoreMapper backendFilterCoreMapper() {
            return new BackendFilterCoreMapperImpl();
        }

        @Bean
        public CommandCoreMapper commandCoreMapper() {
            return new CommandCoreMapperImpl();
        }

        @Bean
        public DomibusCoreMapper domibusCoreMapper() {
            return new DomibusCoreMapperImpl();
        }

        @Bean
        public EArchiveBatchMapper eArchiveBatchMapper() {
            return new EArchiveBatchMapperImpl();
        }

        @Bean
        public MessageCoreMapper messageCoreMapper() {
            return new MessageCoreMapperImpl();
        }

        @Bean
        public PartyCoreMapper partyCoreMapper() {
            return new PartyCoreMapperImpl();
        }
    }

    @Autowired
    protected ObjectService objectService;
}
