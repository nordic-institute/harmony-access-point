package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.SynchronizationService;
import eu.domibus.api.multitenancy.SynchronizedRunnable2;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Ion Perpegel(nperpion)
 * @since 5.0
 */
@Configuration
public class SynchronizedRunnableFactory {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnableFactory.class);

    @Autowired
    SynchronizationService synchronizationService;

    @Bean(autowireCandidate = false)
    @Scope("prototype")
    public SynchronizedRunnable2 createBean(Runnable runnable, String lockKey) {
        return new SynchronizedRunnable2(runnable, lockKey, synchronizationService);
    }
}
