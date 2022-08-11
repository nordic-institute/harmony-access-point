package eu.domibus.core.scheduler;

import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.spi.ThreadExecutor;
import org.springframework.core.task.TaskExecutor;

import static eu.domibus.common.TaskExecutorConstants.DOMIBUS_LONG_RUNNING_TASK_EXECUTOR_BEAN_NAME;

public class DomibusQuartzThreadExecutor implements ThreadExecutor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzThreadExecutor.class);

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread [{}]", thread);
        TaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean(DOMIBUS_LONG_RUNNING_TASK_EXECUTOR_BEAN_NAME, TaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

