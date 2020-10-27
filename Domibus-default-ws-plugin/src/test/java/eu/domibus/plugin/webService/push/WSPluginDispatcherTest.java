package eu.domibus.plugin.webService.push;

import eu.domibus.webservice.backend.BackendApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.soap.SOAPMessage;
import java.util.UUID;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WSPluginDispatcherConfiguration.class})
public class WSPluginDispatcherTest {

    @Autowired
    private WSPluginMessageBuilder wsPluginMessageBuilder;
    @Autowired
    private WSPluginDispatcher wsPluginDispatcher;

    @Before
    public void setUp() {
        //start BackendApplication on localhost:8080
        BackendApplication.main(new String[]{});
    }

    @Test
    public void sendSuccess() {
        SOAPMessage soapMessage = wsPluginDispatcher.dispatch(wsPluginMessageBuilder.buildSOAPMessageSendSuccess(UUID.randomUUID().toString()), "http://localhost:8080/backend");
//        wsPluginDispatcher.getXML(soapMessage);
    }
}