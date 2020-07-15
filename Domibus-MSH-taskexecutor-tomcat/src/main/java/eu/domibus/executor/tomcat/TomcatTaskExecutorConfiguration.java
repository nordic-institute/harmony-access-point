package eu.domibus.executor.tomcat;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class TomcatTaskExecutorConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatTaskExecutorConfiguration.class);

    @Bean(name = {"taskExecutor", "quartzTaskExecutor"})
    public SimpleThreadPoolTaskExecutor simpleThreadPoolTaskExecutor(DomibusPropertyProvider domibusPropertyProvider) {
        SimpleThreadPoolTaskExecutor poolTaskExecutor = new SimpleThreadPoolTaskExecutor();

        Integer threadCount = domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_TASK_EXECUTOR_THREAD_COUNT);
        LOGGER.debug("Configured property [{}] with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_TASK_EXECUTOR_THREAD_COUNT, threadCount);

        poolTaskExecutor.setThreadCount(threadCount);
        return poolTaskExecutor;
    }
}
