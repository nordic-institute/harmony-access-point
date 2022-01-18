package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.security.AuthorizationServiceImpl;
import eu.domibus.core.util.MessageUtil;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class IncomingUserMessageHandlerTest {

    @Tested
    IncomingUserMessageHandler incomingUserMessageHandler;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    @Injectable
    MessageUtil messageUtil;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    Ebms3Converter ebms3Converter;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Injectable
    AttachmentCleanupService attachmentCleanupService;

    @Injectable
    AuthorizationServiceImpl authorizationService;

    /**
     * Happy flow unit testing with actual data
     */
    @Test
    public void testInvoke_tc1Process_HappyFlow(@Injectable Ebms3Messaging messaging,
                                                @Injectable LegConfiguration legConfiguration,
                                                @Injectable final UserMessage userMessage) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{
            soapRequestMessage.getProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            userMessageHandlerService.handleNewUserMessage(legConfiguration, withEqual(pmodeKey), withEqual(soapRequestMessage), withEqual(userMessage), null, null, false);
            result = soapResponseMessage;
        }};

        incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(userMessage, (ErrorResult) any);
            times = 0;
        }};
    }


    /**
     * Unit testing with actual data.
     */
    @Test
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final LegConfiguration legConfiguration,
                                                           @Injectable final Ebms3Messaging messaging,
                                                           @Injectable final UserMessage userMessage) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations() {{

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;

            userMessageHandlerService.handleNewUserMessage(legConfiguration, withAny(pmodeKey), withAny(soapRequestMessage), withAny(userMessage), null, null, false);
            result = EbMS3ExceptionBuilder.getInstance().build();

        }};

        try {
            incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);
            fail();
        } catch (WebServiceException e) {
            //OK
        }

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(userMessage, (ErrorResult) any);
        }};
    }
}
