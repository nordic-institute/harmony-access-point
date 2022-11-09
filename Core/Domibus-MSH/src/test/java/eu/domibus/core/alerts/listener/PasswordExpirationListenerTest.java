package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.listener.generic.RepetitiveEventListener;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PasswordExpirationListenerTest {

    @Tested
    private RepetitiveEventListener passwordExpirationListener;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private EventDao eventDao;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Test
    public void testPasswordEvent() {
        setExpectations();
        passwordExpirationListener.onEvent(new Event(), "default");
        setVerifications();
    }

    void setExpectations() {
        new Expectations() {{
            eventDao.read(anyLong);
            result = new eu.domibus.core.alerts.model.persist.Event();
        }};
    }

    void setVerifications() {
        new VerificationsInOrder() {{
            alertService.createAndEnqueueAlertOnEvent((Event) any);
            times = 1;
        }};
    }
}
