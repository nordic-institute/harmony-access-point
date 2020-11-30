package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.connector.WSPluginImpl;
import eu.domibus.webservice.backend.generated.SendFailure;
import eu.domibus.webservice.backend.generated.SendSuccess;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginMessageBuilderTest {

    public static final String MESSAGE_ID = "messageId";
    @Tested
    private WSPluginMessageBuilder wsPluginMessageBuilder;

    @Injectable
    private XMLUtilExtService xmlUtilExtService;

    @Injectable
    private JAXBContext jaxbContextWebserviceBackend;

    @Injectable
    private WSPluginImpl wsPlugin;

    @Test
    public void getJaxbElement_sendSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder){{
            messageLogEntity.getType();
            result = WSBackendMessageType.SEND_SUCCESS;

            wsPluginMessageBuilder.getSendSuccess(messageLogEntity);
            result = new SendSuccess();
        }};
        wsPluginMessageBuilder.getJaxbElement(messageLogEntity);
    }
    @Test
    public void getJaxbElement_sendFailure(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder){{
            messageLogEntity.getType();
            result = WSBackendMessageType.SEND_FAILURE;

            wsPluginMessageBuilder.getSendFailure(messageLogEntity);
            result = new SendFailure();
        }};
        wsPluginMessageBuilder.getJaxbElement(messageLogEntity);
    }

    @Test
    public void getSendSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(){{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        SendSuccess sendSuccess = wsPluginMessageBuilder.getSendSuccess(messageLogEntity);
        Assert.assertEquals(MESSAGE_ID, sendSuccess.getMessageID());
    }

    @Test
    public void getSendFailure(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(){{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        SendFailure sendFailure = wsPluginMessageBuilder.getSendFailure(messageLogEntity);
        Assert.assertEquals(MESSAGE_ID, sendFailure.getMessageID());
    }
}