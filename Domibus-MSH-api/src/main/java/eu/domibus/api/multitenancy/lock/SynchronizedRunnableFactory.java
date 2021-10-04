package eu.domibus.api.multitenancy.lock;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Configuration
public class SynchronizedRunnableFactory {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnableFactory.class);

    @Autowired
    SynchronizationService synchronizationService;

    @Bean(autowireCandidate = false)
    @Scope("prototype")
    public SynchronizedRunnable createBean(Runnable runnable, String lockKey) {
        return new SynchronizedRunnable(runnable, lockKey, synchronizationService);
    }
}
