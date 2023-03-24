package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import java.util.Base64;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginDispatcherIT extends AbstractBackendWSIT {

    private int backendPort;

    @Autowired
    private WSPluginMessageBuilder wsPluginMessageBuilder;

    @Autowired
    private WSPluginDispatcher wsPluginDispatcher;

    @Test
    @Ignore("[EDELIVERY-8828] WSPLUGIN: tests for rest methods ignored")
    public void sendSuccess() {
        WSBackendMessageLogEntity wsBackendMessageLogEntity = new WSBackendMessageLogEntity();
        wsBackendMessageLogEntity.setMessageId(UUID.randomUUID().toString());
        wsBackendMessageLogEntity.setType(WSBackendMessageType.SEND_SUCCESS);
        SOAPMessage msgToSend = wsPluginMessageBuilder.buildSOAPMessage(wsBackendMessageLogEntity);
        wsPluginDispatcher.dispatch(
                msgToSend,
                "http://localhost:" + backendPort + "/backend");

        String[] authHeader = msgToSend.getMimeHeaders().getHeader("Authorization");
        Assert.assertNotNull(authHeader);
        String decodedCredentials = new String(Base64.getDecoder().decode(authHeader[0].replace("Basic ", "")));
        Assert.assertTrue(decodedCredentials.contains("usertest"));
        Assert.assertTrue(decodedCredentials.contains("passwordtest"));
    }
}