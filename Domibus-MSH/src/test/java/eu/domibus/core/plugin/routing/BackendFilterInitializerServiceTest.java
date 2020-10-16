package eu.domibus.core.plugin.routing;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.plugin.BackendConnector;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class BackendFilterInitializerServiceTest {

    @Tested
    BackendFilterInitializerService backendFilterInitializerService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected BackendConnectorProvider backendConnectorProvider;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected RoutingService routingService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void updateMessageFiltersSingleTenancy(@Injectable BackendConnector backendConnector) {
        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);

        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = true;

            authUtils.runWithSecurityContext((AuthenticatedProcedure) any, anyString, anyString, (AuthRole) any, anyBoolean);
        }};

        backendFilterInitializerService.updateMessageFilters();

        new FullVerifications(authUtils) {{
            AuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            boolean forceSetContext;
            authUtils.runWithSecurityContext(function = withCapture(),
                    username = withCapture(), password = withCapture(), role = withCapture(), forceSetContext = withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("domibus", username);
            Assert.assertEquals("domibus", password);
            Assert.assertEquals(AuthRole.ROLE_AP_ADMIN, role);
            Assert.assertTrue(forceSetContext); // always true for audit reasons
        }};
    }

    @Test
    public void updateMessageFiltersMultiTenancy(@Injectable BackendConnector backendConnector,
                                                 @Injectable Domain domain) {
        List<BackendConnector> backendConnectors = new ArrayList<>();
        backendConnectors.add(backendConnector);

        List<Domain> domains = new ArrayList<>();
        domains.add(domain);

        new Expectations(routingService) {{
            backendConnectorProvider.getBackendConnectors();
            result = backendConnectors;

            domibusConfigurationService.isSingleTenantAware();
            result = false;

            domainService.getDomains();
            result = Collections.singletonList(domain);
        }};

        backendFilterInitializerService.updateMessageFilters();

        new FullVerifications() {{
            domainTaskExecutor.submit((Runnable) any, domain, true, 3L, TimeUnit.MINUTES);
            times = 1;
        }};
    }

    @Test
    public void testInit_noNotificationListenerBeanMap(@Injectable BackendConnectorProvider backendConnectorProvider,
                                                       @Injectable CriteriaFactory routingCriteriaFactory,
                                                       @Injectable BackendFilterEntity backendFilterEntity) {

        RoutingService routingService = new RoutingService();
        routingService.backendConnectorProvider = backendConnectorProvider;

        new Expectations(routingService) {{
        }};

        thrown.expect(ConfigurationException.class);
        backendFilterInitializerService.updateMessageFilters();

        new FullVerifications() {
        };
    }
}