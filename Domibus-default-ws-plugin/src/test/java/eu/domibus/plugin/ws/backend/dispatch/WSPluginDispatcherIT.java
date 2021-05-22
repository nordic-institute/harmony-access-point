package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageType;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@Ignore
public class WSPluginDispatcherIT extends AbstractBackendWSIT {

    private int backendPort;

    @Autowired
    private WSPluginMessageBuilder wsPluginMessageBuilder;

    @Autowired
    private WSPluginDispatcher wsPluginDispatcher;

   /* @Before
    public void setUp() {
        backendPort = SocketUtils.findAvailableTcpPort(3000, 3100);
        BackendApplication.main(new String[]{"" + backendPort});
    }*/

    @Ignore
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