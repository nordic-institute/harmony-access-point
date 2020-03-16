package eu.domibus.core.quartz;

import eu.domibus.core.spring.SpringContextProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

/**
 * @author baciu
 */
@RunWith(JMockit.class)
public class DomibusQuartzThreadExecutorTest {

    @Tested
    DomibusQuartzThreadExecutor domibusQuartzThreadExecutor;

    @Injectable
    ApplicationContext applicationContext;

    @Test
    public void testExecute(final @Injectable Thread thread, final @Injectable TaskExecutor taskExecutor, @Mocked SpringContextProvider springContextProvider) throws Exception {
        new Expectations() {{
            SpringContextProvider.getApplicationContext().getBean("quartzTaskExecutor", TaskExecutor.class);
            result = taskExecutor;
        }};

        domibusQuartzThreadExecutor.execute(thread);

        new Verifications() {{
            taskExecutor.execute(thread);
        }};
    }
}
