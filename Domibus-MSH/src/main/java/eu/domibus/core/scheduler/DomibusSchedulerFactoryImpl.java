package eu.domibus.core.scheduler;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomibusSchedulerFactoryImpl implements DomibusSchedulerFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSchedulerFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Scheduler createScheduler(Domain domain) {
        LOG.debug("Creating the scheduler for domain [{}]", domain);

        Optional<Domain> optionalDomain = Optional.ofNullable(domain);
        return applicationContext.getBean(SchedulerFactoryBean.class, optionalDomain).getScheduler();
    }
}
