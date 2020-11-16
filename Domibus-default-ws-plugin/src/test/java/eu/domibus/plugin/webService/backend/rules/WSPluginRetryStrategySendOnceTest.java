package eu.domibus.plugin.webService.backend.rules;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertNull;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class WSPluginRetryStrategySendOnceTest {
    @Tested
    private WSPluginRetryStrategySendOnce retryStrategySendOnce;

    @Test
    public void calculateNextAttempt_SEND_ONCE() {
        Date nextAttempt = retryStrategySendOnce.calculateNextAttempt(null, 1, 2);
        assertNull(nextAttempt);
    }
}