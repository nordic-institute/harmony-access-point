package eu.domibus.taskexecutor.wildfly;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.enterprise.concurrent.ManagedExecutorService;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class WildFlyTaskExecutorConfiguration {

    public static final String JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_DOMIBUS_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/DomibusExecutorService";
    public static final String JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_MSH_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/MshExecutorService";
    public static final String JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_QUARTZ_EXECUTOR_SERVICE = "java:jboss/ee/concurrency/executor/QuartzExecutorService";

    @Bean("mshExecutorService")
    public DomibusExecutorServiceFactory mshExecutorService() {
        DomibusExecutorServiceFactory mshExecutorServiceFactory = new DomibusExecutorServiceFactory();
        mshExecutorServiceFactory.setExecutorServiceJndiName(JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_MSH_EXECUTOR_SERVICE);
        return mshExecutorServiceFactory;
    }

    @Bean("domibusExecutorService")
    public DomibusExecutorServiceFactory domibusExecutorService() {
        DomibusExecutorServiceFactory domibusExecutorServiceFactory = new DomibusExecutorServiceFactory();
        domibusExecutorServiceFactory.setExecutorServiceJndiName(JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_DOMIBUS_EXECUTOR_SERVICE);
        return domibusExecutorServiceFactory;
    }

    @Bean("quartzExecutorService")
    public DomibusExecutorServiceFactory quartzExecutorService() {
        DomibusExecutorServiceFactory domibusExecutorServiceFactory = new DomibusExecutorServiceFactory();
        domibusExecutorServiceFactory.setExecutorServiceJndiName(JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_QUARTZ_EXECUTOR_SERVICE);
        return domibusExecutorServiceFactory;
    }

    @Bean("taskExecutor")
    public DomibusWildFlyTaskExecutor taskExecutor(@Qualifier("domibusExecutorService") ManagedExecutorService managedExecutorService) {
        DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor = new DomibusWildFlyTaskExecutor();
        domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        return domibusWildFlyTaskExecutor;
    }

    @Bean("mshTaskExecutor")
    public DomibusWildFlyTaskExecutor mshTaskExecutor(@Qualifier("mshExecutorService") ManagedExecutorService managedExecutorService) {
        DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor = new DomibusWildFlyTaskExecutor();
        domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        return domibusWildFlyTaskExecutor;
    }

    @Bean("quartzTaskExecutor")
    public DomibusWildFlyTaskExecutor quartzTaskExecutor(@Qualifier("quartzExecutorService") ManagedExecutorService managedExecutorService) {
        DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor = new DomibusWildFlyTaskExecutor();
        domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        return domibusWildFlyTaskExecutor;
    }
}
