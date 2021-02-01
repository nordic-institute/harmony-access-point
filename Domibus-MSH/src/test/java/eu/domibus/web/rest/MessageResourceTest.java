package eu.domibus.web.rest;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.converter.MessageConverterService;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.MessageLogRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageResourceTest {

    @Tested
    MessageResource messageResource;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    MessageConverterService messageConverterService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private AuditService auditService;

    @Injectable
    MessagesLogService messagesLogService;

    @Injectable
    ErrorHandlerService errorHandlerService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testDownload() {
        // Given
        new Expectations() {{
            userMessageService.getMessageAsBytes(anyString);
            result = new byte[]{0, 1, 2};
        }};

        // When
        ResponseEntity<ByteArrayResource> responseEntity = messageResource.download("messageId");

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(2, responseEntity.getHeaders().size());
        Assert.assertEquals("application/octet-stream", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertEquals("attachment; filename=messageId.xml", responseEntity.getHeaders().get("content-disposition").get(0));
    }

    @Test
    public void testDownloadZipped() throws IOException {
        // Given
        new Expectations() {{
            userMessageService.getMessageWithAttachmentsAsZip(anyString);
            result = new byte[]{0, 1, 2};
        }};

        ResponseEntity<ByteArrayResource> responseEntity = null;
        try {
            // When
            responseEntity = messageResource.downloadUserMessage("messageId");
        } catch (IOException | MessageNotFoundException e) {
            // NOT Then :)
            Assert.fail("Exception in zipFiles method");
        }

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals("application/zip", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertEquals("attachment; filename=messageId.zip", responseEntity.getHeaders().get("content-disposition").get(0));
    }

    @Test
    public void testReSend() {
        String messageId = UUID.randomUUID().toString();
        messageResource.resend(messageId);
        new Verifications() {{
            final String messageIdActual;
            final String messageIdActual1;
            userMessageService.resendFailedOrSendEnqueuedMessage(messageIdActual = withCapture());
            times = 1;
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void test_checkCanDownloadWithDeletedMessage(@Injectable MessageLogRO deletedMessage) {
        new Expectations() {{
            messagesLogService.findUserMessageById(anyString);
            result = deletedMessage;
            deletedMessage.getDeleted();
            result = new Date();

        }};
        try {
            messageResource.checkCanDownload("messageId");
            Assert.fail();
        }catch(MessagingException ex){
            Assert.assertTrue(ex.getMessage().contains("[DOM_001]:Message content is no longer available for message id:"));
        }
    }

    @Test
    public void test_checkCanDownloadWhenNoMessage() {
        new Expectations() {{
            messagesLogService.findUserMessageById(anyString);
            result = null;
        }};

        try {
            messageResource.checkCanDownload("messageId");
            Assert.fail();
        }catch(MessagingException ex){
            Assert.assertEquals(ex.getMessage(),"[DOM_001]:No message found for message id: messageId");
        }
    }

    @Test
    public void test_checkCanDownloadWithExistingMessage(@Injectable MessageLogRO existingMessage) {
        new Expectations() {{
            messagesLogService.findUserMessageById(anyString);
            result = existingMessage;
            existingMessage.getDeleted();
            result = null;
        }};

      messageResource.checkCanDownload("messageId");
    }

    @Test
    public void test_checkCanDownloadWithMaxDownLoadSize(@Injectable MessageLogRO existingMessage) {

        byte[] content = "Message Content".getBytes();

        new Expectations() {{
            messagesLogService.findUserMessageById(anyString);
            result = existingMessage;
            existingMessage.getDeleted();
            result = null;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE);
            result = 1;
            userMessageService.getMessageAsBytes(anyString);
            result = content;
        }};

        try {
            messageResource.checkCanDownload("messageId");
            Assert.fail();
        } catch( MessagingException ex){
            Assert.assertEquals(ex.getMessage(), "[DOM_001]:The message size exceeds maximum download size limit: 1");
        }
    }

    @Test
    public void getByteArrayResourceResponseEntity_empty() {
        String messageId = "messageId";
        new Expectations() {{
            userMessageService.getMessageEnvelopesAsZip(messageId);
            result = new byte[]{};
        }};

        ResponseEntity<ByteArrayResource> result = messageResource.getByteArrayResourceResponseEntity(messageId);
        Assert.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void getByteArrayResourceResponseEntity() {
        String messageId = "messageId";
        byte[] content = {1, 2, 3, 4};
        new Expectations() {{
            userMessageService.getMessageEnvelopesAsZip(messageId);
            this.result = content;
        }};

        ResponseEntity<ByteArrayResource> result = messageResource.getByteArrayResourceResponseEntity(messageId);
        Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assert.assertEquals(content, result.getBody().getByteArray());
        Assert.assertEquals("application/zip", result.getHeaders().get("Content-Type").get(0));
    }

}
