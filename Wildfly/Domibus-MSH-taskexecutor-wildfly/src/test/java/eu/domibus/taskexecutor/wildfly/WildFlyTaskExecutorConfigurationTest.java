package eu.domibus.taskexecutor.wildfly;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.concurrent.ManagedExecutorService;

@RunWith(JMockit.class)
public class WildFlyTaskExecutorConfigurationTest {

    @Tested
    WildFlyTaskExecutorConfiguration wildFlyTaskExecutorConfiguration;

    @Test
    public void domibusExecutorService() {
        DomibusExecutorServiceFactory domibusExecutorServiceFactory = wildFlyTaskExecutorConfiguration.domibusExecutorService();
        Assert.assertEquals(WildFlyTaskExecutorConfiguration.JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_DOMIBUS_EXECUTOR_SERVICE, domibusExecutorServiceFactory.getExecutorServiceJndiName());
    }

    @Test
    public void mshExecutorService() {
        DomibusExecutorServiceFactory mshExecutorServiceFactory = wildFlyTaskExecutorConfiguration.mshExecutorService();
        Assert.assertEquals(WildFlyTaskExecutorConfiguration.JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_MSH_EXECUTOR_SERVICE, mshExecutorServiceFactory.getExecutorServiceJndiName());
    }

    @Test
    public void quartzExecutorService() {
        DomibusExecutorServiceFactory domibusExecutorServiceFactory = wildFlyTaskExecutorConfiguration.quartzExecutorService();
        Assert.assertEquals(WildFlyTaskExecutorConfiguration.JAVA_JBOSS_EE_CONCURRENCY_EXECUTOR_QUARTZ_EXECUTOR_SERVICE, domibusExecutorServiceFactory.getExecutorServiceJndiName());
    }

    @Test
    public void taskExecutor(@Injectable ManagedExecutorService managedExecutorService,
                             @Mocked DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor) {
        wildFlyTaskExecutorConfiguration.taskExecutor(managedExecutorService);

        new Verifications() {{
            domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        }};
    }

    @Test
    public void mshTaskExecutor(@Injectable ManagedExecutorService managedExecutorService,
                             @Mocked DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor) {
        wildFlyTaskExecutorConfiguration.mshTaskExecutor(managedExecutorService);

        new Verifications() {{
            domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        }};
    }

    @Test
    public void quartzTaskExecutor(@Injectable ManagedExecutorService managedExecutorService,
                                   @Mocked DomibusWildFlyTaskExecutor domibusWildFlyTaskExecutor) {
        wildFlyTaskExecutorConfiguration.quartzTaskExecutor(managedExecutorService);

        new Verifications() {{
            domibusWildFlyTaskExecutor.setExecutorService(managedExecutorService);
        }};
    }
}