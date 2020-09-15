package eu.domibus.core.plugin;

import eu.domibus.plugin.BackendConnector;
import mockit.Expectations;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class BackendConnectorProviderTest {

    @Test
    public void getBackendConnector_empty() {
        BackendConnectorProvider backendConnectorProvider = new BackendConnectorProvider(new ArrayList<>());

        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector("mybackend");

        assertNull(backendConnector);
    }

    @Test
    public void getBackendConnector(@Injectable BackendConnector<?, ?> b1,
                                    @Injectable BackendConnector<?, ?> b2,
                                    @Injectable BackendConnector<?, ?> b3) {
        BackendConnectorProvider backendConnectorProvider = new BackendConnectorProvider(asList(b1, b2, b3));
        String backendName = "mybackend";

        new Expectations() {{
            b1.getName();
            result = "b1";

            b2.getName();
            result = "b2";

            b3.getName();
            result = backendName;
        }};

        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);

        assertEquals(b3, backendConnector);
    }

}