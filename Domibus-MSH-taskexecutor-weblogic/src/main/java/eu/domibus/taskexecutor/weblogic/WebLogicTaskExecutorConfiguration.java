package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WebLogicTaskExecutorConfiguration {

    public static final String JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER = "java:comp/env/DomibusWorkManager";
    public static final String JAVA_COMP_ENV_QUARTZ_WORK_MANAGER = "java:comp/env/QuartzWorkManager";

    @Value("domibus.internal.notification.executor")
    private String internalNotificationExecutor;

    @Value("domibus.backend.in.queue.executor")
    private String backendInQueueExecutor;

    @Value("domibus.sending.queue.executor")
    private String sendingQueueExecutor;

    @Value("domibus.dispatcher.executor")
    private String dispatcherExecutor;

    @Bean("internalNotificationExecutor")
    public WorkManagerFactory internalNotificationWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(internalNotificationExecutor)){
            return workManagerFactory;
        }
        WorkManagerFactory backendDispatcherWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(internalNotificationExecutor);
        return backendDispatcherWorkManagerFactory;
    }

    @Bean("backendInQueueExecutor")
    public WorkManagerFactory backendInQueueWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(backendInQueueExecutor)){
            return workManagerFactory;
        }
        WorkManagerFactory backendInQueueManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(backendInQueueExecutor);
        return backendInQueueManagerFactory;
    }

    @Bean("sendingQueueExecutor")
    public WorkManagerFactory sendingQueueWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(sendingQueueExecutor)){
            return workManagerFactory;
        }
        WorkManagerFactory sendingQueueWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(sendingQueueExecutor);
        return sendingQueueWorkManagerFactory;
    }

    @Bean("dispatcherExecutor")
    public WorkManagerFactory dispatcherWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(dispatcherExecutor)){
            return workManagerFactory;
        }
        WorkManagerFactory dispatcherWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(dispatcherExecutor);
        return dispatcherWorkManagerFactory;
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

    @Bean("taskExecutor")
    public DomibusWorkManagerTaskExecutor taskExecutor(@Qualifier("domibusWorkManager") WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(workManager);
        return domibusWorkManagerTaskExecutor;
    }

    @Bean("quartzTaskExecutor")
    public DomibusWorkManagerTaskExecutor quartzTaskExecutor(@Qualifier("quartzWorkManager") WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(workManager);
        return domibusWorkManagerTaskExecutor;
    }
}
