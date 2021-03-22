package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.util.DatabaseUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserAccountListenerTest {

    @Injectable
    private EventService eventService;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Tested
    private UserAccountListener userAccountListener;

    @Test
    public void onLoginFailure(@Mocked final Event event,
                               @Mocked final Alert alert) {
        new Expectations(){{
            databaseUtil.getDatabaseUserName();
            times = 1;
            result = "databaseUserName";
        }};

        userAccountListener.onLoginFailure(event, null);

        new FullVerifications() {{
            domainContextProvider.clearCurrentDomain();
            times = 1;
            eventService.persistEvent(event);
            times = 1;
            alertService.createAlertOnEvent(event);
            times = 1;
            alertService.enqueueAlert(alert);
            times = 1;
        }};
    }

    @Test
    public void onLoginFailure_domain(@Mocked final Event event, @Mocked final Alert alert) {
        String domain = "domain";

        new Expectations(){{
            databaseUtil.getDatabaseUserName();
            times = 1;
            result = "databaseUserName";
        }};

        userAccountListener.onLoginFailure(event, domain);

        new FullVerifications() {{
            domainContextProvider.setCurrentDomain(withAny(domain));
            times = 1;
            eventService.persistEvent(event);
            times = 1;
            alertService.createAlertOnEvent(event);
            times = 1;
            alertService.enqueueAlert(alert);
            times = 1;
        }};
    }
}