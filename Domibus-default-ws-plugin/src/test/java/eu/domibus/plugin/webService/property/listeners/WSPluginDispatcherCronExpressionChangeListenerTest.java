package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.DISPATCHER_CRON_EXPRESSION;
import static eu.domibus.plugin.webService.property.listeners.WSPluginDispatcherCronExpressionChangeListener.SEND_RETRY_JOB_NAME;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class WSPluginDispatcherCronExpressionChangeListenerTest {

    public static final String DEFAULT = "default";
    public static final String REGEX = "*";
    @Mocked
    private DomibusSchedulerExtService domibusSchedulerExtService;

    protected WSPluginDispatcherCronExpressionChangeListener listener;

    @Before
    public void setUp() {
        listener = new WSPluginDispatcherCronExpressionChangeListener(domibusSchedulerExtService);
    }

    @Test
    public void handlesProperty_true() {
        Assert.assertTrue(listener.handlesProperty(DISPATCHER_CRON_EXPRESSION));
    }

    @Test
    public void handlesProperty_false() {
        Assert.assertFalse(listener.handlesProperty("I hate pickles"));
    }

    @Test
    public void propertyValueChanged() {
        listener.propertyValueChanged(DEFAULT, DISPATCHER_CRON_EXPRESSION, REGEX);

        new FullVerifications() {{
            domibusSchedulerExtService.rescheduleJob(DEFAULT, SEND_RETRY_JOB_NAME, REGEX);
            times = 1;
        }};
    }
}