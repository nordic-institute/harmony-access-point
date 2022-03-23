package eu.domibus.core.plugin;

import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class BackendConnectorServiceTest {

    @Tested
    BackendConnectorService backendConnectorService;

    @Injectable
    protected BackendConnectorProviderImpl backendConnectorProvider;

    @Injectable
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;


    @Test
    public void isAbstractBackendConnector(@Injectable AbstractBackendConnector abstractBackendConnector) {
        assertTrue(backendConnectorService.isAbstractBackendConnector(abstractBackendConnector));
    }

    @Test
    public void isAbstractBackendConnector(@Injectable BackendConnector backendConnector) {
        assertFalse(backendConnectorService.isAbstractBackendConnector(backendConnector));
    }

}
