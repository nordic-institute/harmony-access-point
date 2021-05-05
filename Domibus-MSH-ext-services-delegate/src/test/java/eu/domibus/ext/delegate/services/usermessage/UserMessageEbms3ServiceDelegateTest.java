package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.*;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageExtException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(JMockit.class)
public class UserMessageEbms3ServiceDelegateTest {

    public static final String FINAL_RECIPIENT = "finalRecipient";
    @Tested
    UserMessageServiceDelegate userMessageServiceDelegate;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    DomibusExtMapper domibusExtMapper;

    @Injectable
    UserMessageSecurityService userMessageSecurityService;
    public static final String MESSAGE_ID = "messageId";

    @Test
    public void testGetMessageSuccess() {
        // Given
        final String messageId = "messageId";

        final UserMessage userMessage = new UserMessage();
        final MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(messageId);
        messageInfo.setRefToMessageId("refToMessageId");
        messageInfo.setTimestamp(new Date());
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("action");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("pmode");
        agreementRef.setType("type");
        agreementRef.setValue("value");
        collaborationInfo.setAgreementRef(agreementRef);
        collaborationInfo.setConversationId("conversationId");
        Service service = new Service();
        service.setType("type");
        service.setValue("value");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);

        new Expectations() {{
            userMessageService.getMessage(messageId);
            result = userMessage;
        }};


        // When
        userMessageServiceDelegate.getMessage(messageId);

        // Then
        new Verifications() {{
            userMessageService.getMessage(messageId);
            domibusExtMapper.userMessageToUserMessageDTO(userMessage);
        }};
    }

    @Test
    public void testGetMessageException() {
        // Given
        final UserMessageExtException userMessageExtException = new UserMessageExtException(DomibusErrorCode.DOM_001, "test");

        new Expectations() {{
            userMessageService.getMessage(MESSAGE_ID);
            result = userMessageExtException;
        }};

        // When
        try {
            userMessageServiceDelegate.getMessage(MESSAGE_ID);
            Assert.fail();
        } catch (UserMessageExtException e) {
            // Then
            Assert.assertSame(userMessageExtException, e);
        }

    }

    @Test
    public void testGetMessage_null() {
        // Given
        new Expectations() {{
            userMessageService.getMessage(MESSAGE_ID);
            result = null;
        }};

        UserMessageDTO message = userMessageServiceDelegate.getMessage(MESSAGE_ID);
        Assert.assertNull(message);

        new FullVerifications(){{
            userMessageSecurityService.checkMessageAuthorization(MESSAGE_ID);
            times = 1;
        }};
    }

    @Test
    public void getFinalRecipient() {
        new Expectations() {{
            userMessageService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = FINAL_RECIPIENT;
        }};

        String finalRecipient = userMessageServiceDelegate.getFinalRecipient(MESSAGE_ID);
        Assert.assertEquals(FINAL_RECIPIENT, finalRecipient);
        new FullVerifications() {{
            userMessageSecurityService.checkMessageAuthorization(MESSAGE_ID);
            times = 1;
        }};
    }

    @Test
    public void getUserMessageEnvelope() {
        new Expectations() {{
            userMessageService.getUserMessageEnvelope(MESSAGE_ID);
            times = 1;
            result = FINAL_RECIPIENT;
        }};

        String finalRecipient = userMessageServiceDelegate.getUserMessageEnvelope(MESSAGE_ID);
        Assert.assertEquals(FINAL_RECIPIENT, finalRecipient);
        new FullVerifications() {{
            userMessageSecurityService.checkMessageAuthorization(MESSAGE_ID);
            times = 1;
        }};
    }

    @Test
    public void getSignalMessageEnvelope() {
        new Expectations() {{
            userMessageService.getSignalMessageEnvelope(MESSAGE_ID);
            times = 1;
            result = FINAL_RECIPIENT;
        }};

        String finalRecipient = userMessageServiceDelegate.getSignalMessageEnvelope(MESSAGE_ID);
        Assert.assertEquals(FINAL_RECIPIENT, finalRecipient);
        new FullVerifications() {{
            userMessageSecurityService.checkMessageAuthorization(MESSAGE_ID);
            times = 1;
        }};
    }
}
