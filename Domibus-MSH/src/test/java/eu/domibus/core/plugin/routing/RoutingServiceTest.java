package eu.domibus.core.plugin.routing;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.core.plugin.routing.dao.BackendFilterDao;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * // TODO reach 70% coverage.
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class RoutingServiceTest {

    @Tested
    RoutingService routingService;

    @Injectable
    private BackendFilterDao backendFilterDao;

    @Injectable
    private List<NotificationListener> notificationListeners;

    @Injectable
    private DomainCoreConverter coreConverter;

    @Injectable
    BackendNotificationService backendNotificationService;


    @Test(expected = ConfigurationException.class)
    public void validateFiltersThrowsError() {
        BackendFilter bf1 = new BackendFilter();
        bf1.setBackendName("wsPlugin");
        List<RoutingCriteria> rc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf1.setRoutingCriterias(rc);

        BackendFilter bf2 = new BackendFilter();
        bf2.setBackendName("wsPlugin");
        List<RoutingCriteria> rcc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf2.setRoutingCriterias(rcc);

        BackendFilter bf3 = new BackendFilter();
        bf3.setBackendName("wsPlugin");
        List<RoutingCriteria> rcd = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc3Name");
                    setExpression("rc3Expression");
                }});
        bf3.setRoutingCriterias(rcd);

        routingService.validateFilters(Arrays.asList(bf1, bf2, bf3));
    }

    @Test()
    public void validateFilters() {
        BackendFilter bf1 = new BackendFilter();
        bf1.setBackendName("wsPlugin");
        List<RoutingCriteria> rc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2Expression");
                }});
        bf1.setRoutingCriterias(rc);

        BackendFilter bf2 = new BackendFilter();
        bf2.setBackendName("wsPlugin");
        List<RoutingCriteria> rcc = Arrays.asList(
                new RoutingCriteria() {{
                    setName("rc1Name");
                    setExpression("rc1Expression");
                }},
                new RoutingCriteria() {{
                    setName("rc2Name");
                    setExpression("rc2ExpressionDifferent");
                }});
        bf2.setRoutingCriterias(rcc);

        routingService.validateFilters(Arrays.asList(bf1, bf2));
    }
}