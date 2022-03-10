package eu.domibus.core.message;


import eu.domibus.ITTestsService;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author idragusa
 * @since 5.0
 */
@Transactional
public class DeleteSentSuccessMessageIT extends DeleteMessageAbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeleteSentSuccessMessageIT.class);

    @Autowired
    private ITTestsService itTestsService;

    @Autowired
    private UserMessageDao userMessageDao;
    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Before
    public void updatePmodeForAcknowledged() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("security=\"eDeliveryAS4Policy\"", "security=\"noSigNoEnc\"");
        uploadPmode(SERVICE_PORT, toReplace);
    }

    /**
     * Test to delete a sent success message
     */
    @Test
    public void testDeleteSentMessage() throws MessagingProcessingException, IOException {
        deleteAllMessages();

        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        String messageId = itTestsService.sendMessageWithStatus(MessageStatus.ACKNOWLEDGED);

        LOG.info("Message Id to delete: [{}]", messageId);
        UserMessage byMessageId = userMessageDao.findByMessageId(messageId);
        Assert.assertNotNull(byMessageId);

        Assert.assertNotNull(userMessageDao.findByEntityId(byMessageId.getEntityId()));
        Assert.assertNotNull(userMessageLogDao.findByEntityIdSafely(byMessageId.getEntityId()));

        em.flush();
        em.clear();
        deleteAllMessages();

        Assert.assertNull(userMessageDao.findByMessageId(messageId));
        try {
            userMessageLogDao.findByMessageId(messageId);
            Assert.fail();
        } catch (NoResultException e) {
            //OK
        }
    }
}
