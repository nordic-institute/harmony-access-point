package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeDto;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLog;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.ebms3.common.model.UserMessage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class RawEnvelopeLogDaoTestIT extends AbstractIT {

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    public void findUserMessageEnvelopeById() {
        UserMessage userMessage = new UserMessage();
        userMessage.setEntityId(111L);

        RawEnvelopeLog envelope = new RawEnvelopeLog();
        envelope.setMessageId("1111");
        envelope.setRawXML("raw xml");
        envelope.setUserMessage(userMessage);

        rawEnvelopeLogDao.create(envelope);

        RawEnvelopeDto result = rawEnvelopeLogDao.findUserMessageEnvelopeById(1111L);

        assertEquals(1111L, result.getId());
    }
}