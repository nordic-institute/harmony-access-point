package eu.domibus.taskexecutor.weblogic;

import commonj.work.WorkManager;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class WebLogicTaskExecutorConfigurationTest {

    @Tested
    WebLogicTaskExecutorConfiguration webLogicTaskExecutorConfiguration;

    @Test
    public void workManagerFactory() {
        WorkManagerFactory workManagerFactory = webLogicTaskExecutorConfiguration.workManagerFactory();
        Assert.assertEquals(WebLogicTaskExecutorConfiguration.JAVA_COMP_ENV_DOMIBUS_WORK_MANAGER, workManagerFactory.getWorkManagerJndiName());
    }

    @Test
    public void quartzWorkManager() {
        WorkManagerFactory workManagerFactory = webLogicTaskExecutorConfiguration.quartzWorkManager();
        Assert.assertEquals(WebLogicTaskExecutorConfiguration.JAVA_COMP_ENV_QUARTZ_WORK_MANAGER, workManagerFactory.getWorkManagerJndiName());
    }

    @Test
    public void mshWorkManager() {
        WorkManagerFactory workManagerFactory = webLogicTaskExecutorConfiguration.mshWorkManagerFactory();
        Assert.assertEquals(WebLogicTaskExecutorConfiguration.JAVA_COMP_ENV_MSH_WORK_MANAGER, workManagerFactory.getWorkManagerJndiName());
    }


    @Test
    public void taskExecutor(@Injectable WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = webLogicTaskExecutorConfiguration.taskExecutor(workManager);
        Assert.assertEquals(domibusWorkManagerTaskExecutor.workManager, workManager);
    }

    @Test
    public void quartzTaskExecutor(@Injectable WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = webLogicTaskExecutorConfiguration.quartzTaskExecutor(workManager);
        Assert.assertEquals(domibusWorkManagerTaskExecutor.workManager, workManager);
    }

    @Test
    public void mshTaskExecutor(@Injectable WorkManager workManager) {
        DomibusWorkManagerTaskExecutor domibusWorkManagerTaskExecutor = webLogicTaskExecutorConfiguration.mshTaskExecutor(workManager);
        Assert.assertEquals(domibusWorkManagerTaskExecutor.workManager, workManager);
    }
}