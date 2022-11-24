package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static eu.domibus.common.TaskExecutorConstants.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WebLogicTaskExecutorConfiguration {

    public static final String JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER = "java:comp/env/DomibusWorkManager";
    public static final String JAVA_COMP_ENV_MSH_WORK_MANAGER = "java:comp/env/MshWorkManager";
    public static final String JAVA_COMP_ENV_QUARTZ_WORK_MANAGER = "java:comp/env/QuartzWorkManager";


    @Bean("mshWorkManager")
    public WorkManagerFactory mshWorkManagerFactory() {
        WorkManagerFactory workManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(JAVA_COMP_ENV_MSH_WORK_MANAGER);
        return workManagerFactory;
    }

    @Bean("domibusWorkManager")
    public WorkManagerFactory workManagerFactory() {
        WorkManagerFactory workManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER);
        return workManagerFactory;
    }

    @Bean("quartzWorkManager")
    public WorkManagerFactory quartzWorkManager() {
        WorkManagerFactory workManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(JAVA_COMP_ENV_QUARTZ_WORK_MANAGER);
        return workManagerFactory;
    }

    @Bean(DOMIBUS_MSH_TASK_EXECUTOR_BEAN_NAME)
    public DomibusWorkManagerTaskExecutor mshTaskExecutor(@Qualifier("mshWorkManager") WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(workManager);
        return domibusWorkManagerTaskExecutor;
    }

    @Bean(DOMIBUS_TASK_EXECUTOR_BEAN_NAME)
    public DomibusWorkManagerTaskExecutor taskExecutor(@Qualifier("domibusWorkManager") WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(workManager);
        return domibusWorkManagerTaskExecutor;
    }

    @Bean(DOMIBUS_LONG_RUNNING_TASK_EXECUTOR_BEAN_NAME)
    public DomibusWorkManagerTaskExecutor quartzTaskExecutor(@Qualifier("quartzWorkManager") WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(workManager);
        return domibusWorkManagerTaskExecutor;
    }
}
