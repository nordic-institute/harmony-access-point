package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageType;
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
@ContextConfiguration(classes = {WSPluginDispatcherTestConfiguration.class})
public class WSPluginDispatcherTest {

    private int backendPort;
    @Autowired
    private WSPluginMessageBuilder wsPluginMessageBuilder;
    @Autowired
    private WSPluginDispatcher wsPluginDispatcher;

    @Before
    public void setUp() {
        backendPort = SocketUtils.findAvailableTcpPort(3000, 3100);
        BackendApplication.main(new String[]{"" + backendPort});
    }

    @Test
    public void sendSuccess() {
        WSBackendMessageLogEntity wsBackendMessageLogEntity = new WSBackendMessageLogEntity();
        wsBackendMessageLogEntity.setMessageId(UUID.randomUUID().toString());
        wsBackendMessageLogEntity.setType(WSBackendMessageType.SEND_SUCCESS);
        wsPluginDispatcher.dispatch(
                wsPluginMessageBuilder.buildSOAPMessage(wsBackendMessageLogEntity),
                "http://localhost:" + backendPort + "/backend");
    }
}