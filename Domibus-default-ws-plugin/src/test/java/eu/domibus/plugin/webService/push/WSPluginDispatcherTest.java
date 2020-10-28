package eu.domibus.plugin.webService.push;

import eu.domibus.webservice.backend.BackendApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WSPluginDispatcherConfiguration.class})
public class WSPluginDispatcherTest {

    private int backendPort;
    @Autowired
    private WSPluginMessageBuilder wsPluginMessageBuilder;
    @Autowired
    private WSPluginDispatcher wsPluginDispatcher;

    @Before
    public void setUp() {
        backendPort = SocketUtils.findAvailableTcpPort(3000, 3100);
        BackendApplication.main(backendPort, new String[]{});
    }

    @Test
    public void sendSuccess() {
        wsPluginDispatcher.dispatch(
                wsPluginMessageBuilder.buildSOAPMessageSendSuccess(UUID.randomUUID().toString()),
                "http://localhost:" + backendPort + "/backend");
    }
}