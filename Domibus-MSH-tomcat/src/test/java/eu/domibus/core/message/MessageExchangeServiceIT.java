package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.web.rest.ro.MessageLogRO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
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
        messageDaoTestUtil.createUserMessageLog(messageId, new Date(), MSHRole.RECEIVING, MessageStatus.RECEIVED);

        String testXMLEnvelope = "testXMLEnvelope";
        messageExchangeService.saveRawXml(testXMLEnvelope, messageId);
        RawEnvelopeDto rawXmlByMessageId = userMessageRawEnvelopeDao.findRawXmlByMessageId(messageId);
        String rawXmlMessage = rawXmlByMessageId.getRawXmlMessage();
        Assert.assertEquals(testXMLEnvelope, rawXmlMessage);
    }

}
