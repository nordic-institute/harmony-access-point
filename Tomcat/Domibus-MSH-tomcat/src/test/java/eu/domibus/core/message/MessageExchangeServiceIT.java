package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.UserMessageRaw;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class MessageExchangeServiceIT extends AbstractIT {

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    MessageExchangeService messageExchangeService;

    @Autowired
    UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;

    @Test
    @Transactional
    public void findUserMessageById() {

        String messageId = "msg1";
        String testXMLEnvelope = "testXMLEnvelope";

        messageDaoTestUtil.createUserMessageLog(messageId, new Date(), MSHRole.RECEIVING, MessageStatus.RECEIVED);
        messageExchangeService.saveRawXml(testXMLEnvelope, messageId, MSHRole.RECEIVING);
        RawEnvelopeDto rawXmlByMessageId = userMessageRawEnvelopeDao.findRawXmlByMessageId(messageId, MSHRole.RECEIVING);
        String rawXmlMessage = rawXmlByMessageId.getRawXmlMessage();
        Assert.assertEquals(testXMLEnvelope, rawXmlMessage);

        UserMessageRaw byReference = userMessageRawEnvelopeDao.findByReference(rawXmlByMessageId.getId());
        Assert.assertTrue(Arrays.equals(byReference.getRawXML(), testXMLEnvelope.getBytes(StandardCharsets.UTF_8)));
    }

}
