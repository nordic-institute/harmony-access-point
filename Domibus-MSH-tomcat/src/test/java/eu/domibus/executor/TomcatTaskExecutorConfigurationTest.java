package eu.domibus.executor;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.executor.tomcat.TomcatTaskExecutorConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class TomcatTaskExecutorConfigurationTest {

    @Tested
    TomcatTaskExecutorConfiguration tomcatTaskExecutorConfiguration;

    @Test
    public void simpleThreadPoolTaskExecutor(@Injectable DomibusPropertyProvider domibusPropertyProvider,
                                             @Mocked SimpleThreadPoolTaskExecutor poolTaskExecutor) {
        int threadCount = 20;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_TASK_EXECUTOR_THREAD_COUNT);
            this.result = threadCount;
        }};

        tomcatTaskExecutorConfiguration.simpleThreadPoolTaskExecutor(domibusPropertyProvider);

        new Verifications() {{
            poolTaskExecutor.setThreadCount(threadCount);
        }};
    }
}