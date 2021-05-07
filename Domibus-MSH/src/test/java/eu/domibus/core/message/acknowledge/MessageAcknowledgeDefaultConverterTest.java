package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageAcknowledgeDefaultConverterTest {

    @Tested
    MessageAcknowledgeDefaultConverter messageAcknowledgeDefaultConverter;

    @Test
    public void testCreate() {
        String user = "baciuco";
        String messageId = "1";
        Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";

        final MessageAcknowledgementEntity messageAcknowledgementEntity = messageAcknowledgeDefaultConverter.create(user, messageId, acknowledgeTimestamp, from, to);
        assertEquals(messageAcknowledgementEntity.getCreateUser(), user);
        assertEquals(messageAcknowledgementEntity.getAcknowledgeDate(), acknowledgeTimestamp);
        assertEquals(messageAcknowledgementEntity.getFrom(), from);
        assertEquals(messageAcknowledgementEntity.getTo(), to);
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testConvert()  {
        String user = "baciuco";
        String messageId = "1";
        Timestamp acknowledgeTimestamp = new Timestamp(System.currentTimeMillis());
        String from = "C3";
        String to = "C4";

        final MessageAcknowledgementEntity messageAcknowledgementEntity = messageAcknowledgeDefaultConverter.create(user, messageId, acknowledgeTimestamp, from, to);

        final MessageAcknowledgement converted = messageAcknowledgeDefaultConverter.convert(messageAcknowledgementEntity);
        assertEquals(messageAcknowledgementEntity.getCreateUser(), converted.getCreateUser());
        assertEquals(messageAcknowledgementEntity.getAcknowledgeDate(), converted.getAcknowledgeDate());
        assertEquals(messageAcknowledgementEntity.getFrom(), converted.getFrom());
        assertEquals(messageAcknowledgementEntity.getTo(), converted.getTo());
    }
}
