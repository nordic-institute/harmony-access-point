package eu.domibus.core.message.attempt;

import eu.domibus.AbstractIT;
import eu.domibus.ITTestsService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Transactional
public class MessageAttemptDaoTestIT extends AbstractIT {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    @Autowired
    MessageAttemptDao messageAttemptDao;
    @Autowired
    ITTestsService itTestsService;

    @Before
    public void setup() {
        LOG.debug("Setting up");
    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge() {

        MessageAttemptEntity entity = new MessageAttemptEntity();
        UserMessage userMessage = itTestsService.getUserMessage();
        entity.setUserMessage(userMessage);
        entity.setStartDate(new Timestamp(System.currentTimeMillis()));
        entity.setEndDate(new Timestamp(System.currentTimeMillis()));
        entity.setStatus(MessageAttemptStatus.SUCCESS);

        messageAttemptDao.create(entity);
        assertTrue(entity.getEntityId() > 0);

        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId(userMessage.getMessageId());
        assertEquals(1, entities.size());
        MessageAttemptEntity next = entities.iterator().next();
        assertEquals(entity, next);
        assertNotNull(next.getCreationTime());
        assertNotNull(next.getModificationTime());
        assertNotNull(next.getCreatedBy());
        assertNotNull(next.getModifiedBy());

        assertEquals(next.getCreationTime(), next.getModificationTime());

    }

    @Test
    @Transactional
    public void testSaveMessageAcknowledge_notFound() {
        final List<MessageAttemptEntity> entities = messageAttemptDao.findByMessageId("not_found");
        assertEquals(0, entities.size());
    }
}

