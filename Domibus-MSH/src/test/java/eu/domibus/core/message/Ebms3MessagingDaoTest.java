package eu.domibus.core.message;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.support.DataAccessUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.function.Predicate;

public class Ebms3MessagingDaoTest {

    @Tested
    private MessagingDao messagingDao;

    @Injectable
    private EntityManager entityManager;

    @Mocked
    TypedQuery<SignalMessage> query;

    @Test
    public void testFindSignalMessageByMessageId() {
        // Given
        SignalMessage signalMessage = new SignalMessage();
        String signalMessageId = "test";
        new Expectations() {{
           entityManager.createNamedQuery(anyString, SignalMessage.class);
           result = query;
           DataAccessUtils.singleResult(query.getResultList());
           result = signalMessage;
        }};

        // When
        SignalMessage signalMessageByMessageId = messagingDao.findSignalMessageByMessageId(signalMessageId);

        // Then
        Assert.assertEquals(signalMessage, signalMessageByMessageId);
    }

    @Test
    public void getPayloads() {
    }

    @Test
    public void getFilenameEmptyPredicate(@Injectable PartInfo partInfo) {
        new Expectations() {{
           partInfo.getFileName();
           result = "/home/filename";
        }};

        Predicate<PartInfo> filenameEmptyPredicate = messagingDao.getFilenameEmptyPredicate();
        Assert.assertFalse(filenameEmptyPredicate.test(partInfo));
    }

    @Test
    public void getDatabasePayloads(@Injectable final UserMessage userMessage,
                                    @Injectable Predicate<PartInfo> filenameEmptyPredicate) {
        new Expectations(messagingDao) {{
            messagingDao.getFilenameEmptyPredicate();
            result  = filenameEmptyPredicate;
        }};

        messagingDao.getDatabasePayloads(userMessage);

        new Verifications() {{
            messagingDao.getPayloads(userMessage, filenameEmptyPredicate);
        }};
    }

    @Test
    public void getFileSystemPayloads(@Injectable final UserMessage userMessage,
                                      @Injectable Predicate<PartInfo> filenameEmptyPredicate,
                                      @Injectable Predicate<PartInfo> filenamePresentPredicate) {
        new Expectations(messagingDao) {{
            messagingDao.getFilenameEmptyPredicate();
            result  = filenameEmptyPredicate;

            filenameEmptyPredicate.negate();
            result = filenamePresentPredicate;
        }};

        messagingDao.getFileSystemPayloads(userMessage);

        new Verifications() {{
            filenameEmptyPredicate.negate();
            messagingDao.getPayloads(userMessage, filenamePresentPredicate);
        }};
    }
}
