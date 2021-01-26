package eu.domibus.core.message.acknowledge;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.Messaging;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.ebms3.Ebms3MessagingDocumentParser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
//@ActiveProfiles("h2Debug")
//TODO move it in the core module when Domibus will reference the configuration files via the classpath instead of file disk
public class MessageAcknowledgementDaoTestIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    Ebms3MessagingDocumentParser ebms3MessagingDocumentParser;

    @Autowired
    Ebms3Converter ebms3Converter;

    @Before
    public void setup() {
        LOG.debug("Setting up");
    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge() {
        String user = "baciuco";
        String messageId = "123";
        Timestamp acknowledgetTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";
        Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop1", "value1");

        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, messageId, acknowledgetTimestamp, from, to, properties);
        messageAcknowledgementDao.create(entity);

        final List<MessageAcknowledgementEntity> retrievedEntityList = messageAcknowledgementDao.findByMessageId(messageId);

        assertNotNull(retrievedEntityList);
        assertEquals(1, retrievedEntityList.size());

        final MessageAcknowledgementEntity retrievedEntity = retrievedEntityList.get(0);
        assertEquals(entity.getEntityId(), retrievedEntity.getEntityId());
        assertEquals(entity.getCreateUser(), retrievedEntity.getCreateUser());
        assertEquals(entity.getMessageId(), retrievedEntity.getMessageId());
        assertEquals(entity.getAcknowledgeDate(), retrievedEntity.getAcknowledgeDate());
        assertEquals(entity.getFrom(), retrievedEntity.getFrom());
        assertEquals(entity.getTo(), retrievedEntity.getTo());
        assertEquals(entity.getProperties().iterator().next(), retrievedEntity.getProperties().iterator().next());

        assertNotNull(entity.getCreationTime());
        assertNotNull(entity.getCreatedBy());
        assertNotNull(entity.getModificationTime());
        assertNotNull(entity.getModifiedBy());

        assertEquals(entity.getCreationTime().getTime(), entity.getModificationTime().getTime());
    }

    //    @Test
//    @Transactional
    public void testMessaging() throws Exception {
        //TODO: Check why Party From and To are not working
        Ebms3SignalMessage signalMessage = getSignalMessage();

        Ebms3Messaging ebms3Messaging = getMessaging();
        ebms3Messaging.setSignalMessage(signalMessage);
        ebms3Messaging.getUserMessage().setMessageInfo(signalMessage.getMessageInfo());

        Messaging messaging = ebms3Converter.convertFromEbms3(ebms3Messaging);
        messagingDao.create(messaging);

        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(ebms3Messaging.getSignalMessage().getMessageInfo().getMessageId())
                .setMessageStatus(MessageStatus.SEND_IN_PROGRESS)
                .setMshRole(MSHRole.SENDING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        // Saves an entry of the signal message log
        signalMessageLogDao.create(smlBuilder.build());

        List<MessageLogInfo> allInfoPaged = signalMessageLogDao.findAllInfoPaged(1, 100, null, false, new HashMap<String, Object>());
        System.out.println("results:" + allInfoPaged);
    }

    protected Ebms3SignalMessage getSignalMessage() throws Exception {
        String resource = "dataset/as4/validAS4Response.xml";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        Ebms3Messaging ebms3Messaging = ebms3MessagingDocumentParser.parseMessaging(is, "eb3");
        return ebms3Messaging.getSignalMessage();
    }

    protected Ebms3Messaging getMessaging() throws Exception {
        String resource = "dataset/as4/blue2redGoodMessage.xml";

        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        return ebms3MessagingDocumentParser.parseMessaging(is, "ns");
    }

}

