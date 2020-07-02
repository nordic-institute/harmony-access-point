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

    @Value("${domibus.internal.notification.work.manager.name}")
    private String internalNotificationWorkManagerName;

    @Value("${domibus.backend.in.queue.work.manager.name}")
    private String backendInQueueWorkManagerName;

    @Value("${domibus.sending.queue.work.manager.name}")
    private String sendingQueueWorkManagerName;

    @Value("${domibus.dispatcher.work.manager.name}")
    private String dispatcherWorkManagerName;

    @Bean("internalNotificationWorkManager")
    public WorkManagerFactory internalNotificationWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(internalNotificationWorkManagerName)){
            return workManagerFactory;
        }
        WorkManagerFactory backendDispatcherWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(internalNotificationWorkManagerName);
        return backendDispatcherWorkManagerFactory;
    }

    @Bean("backendInQueueWorkManager")
    public WorkManagerFactory backendInQueueWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(backendInQueueWorkManagerName)){
            return workManagerFactory;
        }
        WorkManagerFactory backendInQueueManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(backendInQueueWorkManagerName);
        return backendInQueueManagerFactory;
    }

    @Bean("sendingQueueWorkManager")
    public WorkManagerFactory sendingQueueWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(sendingQueueWorkManagerName)){
            return workManagerFactory;
        }
        WorkManagerFactory sendingQueueWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(sendingQueueWorkManagerName);
        return sendingQueueWorkManagerFactory;
    }

    @Bean("dispatcherWorkManager")
    public WorkManagerFactory dispatcherWorkManagerFactory(@Qualifier("domibusWorkManager") WorkManagerFactory workManagerFactory) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(dispatcherWorkManagerName)){
            return workManagerFactory;
        }
        WorkManagerFactory dispatcherWorkManagerFactory = new WorkManagerFactory();
        workManagerFactory.setWorkManagerJndiName(dispatcherWorkManagerName);
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

    @Bean("internalNotificationWorkExecutor")
    public DomibusWorkManagerTaskExecutor internalNotificationWorkExecutor(@Qualifier("taskExecutor") DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor,@Qualifier("internalNotificationWorkManager") WorkManager internalNotificationWorkManager) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(internalNotificationWorkManagerName)){
            return domibusWorkManagerTaskExecutor;
        }
        DomibusWorkManagerTaskExecutor internalNotificationWorkExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(internalNotificationWorkManager);
        return internalNotificationWorkExecutor;
    }

    @Bean("backendInQueueWorkExecutor")
    public DomibusWorkManagerTaskExecutor backendInQueueWorkExecutor(@Qualifier("taskExecutor") DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor,@Qualifier("backendInQueueWorkManager") WorkManager backendInQueueWorkManager) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(backendInQueueWorkManagerName)){
            return domibusWorkManagerTaskExecutor;
        }
        DomibusWorkManagerTaskExecutor backendInQueueWorkExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(backendInQueueWorkManager);
        return backendInQueueWorkExecutor;
    }

    @Bean("sendingQueueWorkExecutor")
    public DomibusWorkManagerTaskExecutor sendingQueueWorkExecutor(@Qualifier("taskExecutor") DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor,@Qualifier("sendingQueueWorkManager") WorkManager sendingQueueWorkManager) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(sendingQueueWorkManagerName)){
            return domibusWorkManagerTaskExecutor;
        }
        DomibusWorkManagerTaskExecutor sendingQueueWorkExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(sendingQueueWorkManager);
        return sendingQueueWorkExecutor;
    }

    @Bean("dispatcherWorkExecutor")
    public DomibusWorkManagerTaskExecutor dispatcherWorkExecutor(@Qualifier("taskExecutor") DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor,@Qualifier("dispatcherWorkManager") WorkManager dispatcherWorkManager) {
        if(JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER.equals(dispatcherWorkManagerName)){
            return domibusWorkManagerTaskExecutor;
        }
        DomibusWorkManagerTaskExecutor dispatcherWorkExecutor = new DomibusWorkManagerTaskExecutor();
        domibusWorkManagerTaskExecutor.setWorkManager(dispatcherWorkManager);
        return dispatcherWorkExecutor;
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
