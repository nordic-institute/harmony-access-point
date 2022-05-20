package eu.domibus.core.crypto.spi.dss.listeners;

import eu.domibus.ext.services.DomibusSchedulerExtService;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.AUTHENTICATION_DSS_REFRESH_CRON;
import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
@RunWith(JMockit.class)
public class TriggerChangeListenerTest {

    @Test
    public void handlesProperty(@Mocked DomibusSchedulerExtService domibusSchedulerExtService) {
        TriggerChangeListener triggerChangeListener = new TriggerChangeListener(domibusSchedulerExtService);
        assertTrue(triggerChangeListener.handlesProperty(AUTHENTICATION_DSS_REFRESH_CRON));
        assertFalse(triggerChangeListener.handlesProperty("other property"));
    }

    @Test
    public void propertyValueChanged(@Mocked DomibusSchedulerExtService domibusSchedulerExtService) {
        String domain = "domain";
        String cronExpression = "cronExpression";
        TriggerChangeListener triggerChangeListener = new TriggerChangeListener(domibusSchedulerExtService);
        triggerChangeListener.propertyValueChanged(domain,AUTHENTICATION_DSS_REFRESH_CRON, cronExpression);
        new Verifications(){{
           domibusSchedulerExtService.rescheduleJob(domain,"dssRefreshJob",cronExpression);
        }};
    }
}