package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageState;
import eu.domibus.api.util.DateUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class MessagingLockDaoImplTest {

    @Injectable
    private DateUtil dateUtil;

    @Injectable
    private EntityManager entityManager;

    @Injectable
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Tested
    private MessagingLockDaoImpl messagingLockDao;

    @Test
    public void getNextPullMessageToProcessFirstAttempt(
            @Mocked final Query query,@Mocked final MessagingLock messagingLock) {
        final long idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 0;

        final int sendAttemptsMax = 5;
        final String mpc = "mpc", initiator = "domibus-red";

        final Date date=new Date(System.currentTimeMillis()+20000);
        createExpectation(query, messagingLock, messageId, sendAttempts, sendAttemptsMax, date);
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(mpc, initiator);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.FIRST_ATTEMPT, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());

        new Verifications() {{
            MessageState messageState;
            messagingLock.setMessageState(messageState=withCapture());
            assertEquals(MessageState.PROCESS,messageState);

        }};
    }



    @Test
    public void getNextPullMessageToProcessWithRetry(
            @Mocked final Query query,@Mocked final MessagingLock messagingLock) {
        final long idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 1;

        final int sendAttemptsMax = 5;
        final String mpc = "mpc", initiator = "domibus-red";

        final Date date=new Date(System.currentTimeMillis()+10000);
        createExpectation(query, messagingLock, messageId, sendAttempts, sendAttemptsMax, date);
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(mpc,initiator);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.RETRY, nextPullMessageToProcess.getState());
        assertEquals(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            MessageState messageState;
            messagingLock.setMessageState(messageState=withCapture());
            assertEquals(MessageState.PROCESS,messageState);

        }};
    }

    @Test
    public void getNextPullMessageToProcessExpired(
            @Mocked final Query query,@Mocked final MessagingLock messagingLock) {

        final long idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 1;

        final int sendAttemptsMax = 5;

        final String mpc = "mpc", initiator = "domibus-red";

        final Date date=new Date(System.currentTimeMillis()-20000);

        createExpectation(query, messagingLock, messageId, sendAttempts, sendAttemptsMax, date);
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(mpc, initiator);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertNotNull(null, nextPullMessageToProcess.getStaledReason());


        new Verifications() {{
            MessageState messageState;
            messagingLock.setMessageState(messageState=withCapture());
            assertEquals(MessageState.DEL,messageState);

        }};
    }

    @Test
    public void getNextPullMessageToProcessMaxAttemptsReached(
            @Mocked final Query query,@Mocked final MessagingLock messagingLock) {
        final long idPk = 6;

        final String messageId = "furtherAttemptMessageId";

        final int sendAttempts = 5;

        final int sendAttemptsMax = 5;

        final String mpc = "mpc", initiator = "domibus-red";

        createExpectation(query, messagingLock, messageId, sendAttempts, sendAttemptsMax, new Date());
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(mpc, initiator);
        assertNotNull(nextPullMessageToProcess);
        assertEquals(messageId, nextPullMessageToProcess.getMessageId());
        assertEquals(PullMessageState.EXPIRED, nextPullMessageToProcess.getState());
        assertNotNull(null, nextPullMessageToProcess.getStaledReason());



        new Verifications() {{
            MessageState messageState;
            messagingLock.setMessageState(messageState=withCapture());
            assertEquals(MessageState.DEL,messageState);

        }};
    }



    @Test
    public void getNextPullMessageToProcessNoMessage(@Mocked final Query query) {
        final long idPk = 6;
        final String mpc = "mpc", initiator = "domibus-red";
        new Expectations() {{
            entityManager.createNativeQuery(MessagingLockDaoImpl.LOCK_QUERY_SKIP_LOCKED_ORACLE, MessagingLock.class);
            result = query;

            query.getSingleResult();
            result=new NoResultException();
        }};
        final PullMessageId nextPullMessageToProcess = messagingLockDao.getNextPullMessageToProcess(mpc, initiator);
        assertNull(nextPullMessageToProcess);
    }

    @Test
    public void delete(@Mocked final Query query) {
        final String messageId = "messageId";
        new Expectations() {{
            entityManager.createNamedQuery("MessagingLock.delete");
            result = query;
        }};
        messagingLockDao.delete(messageId);
        new Verifications() {{
            query.setParameter("MESSAGE_ID", messageId);
            query.executeUpdate();
        }};

    }

    private void createExpectation(@Mocked Query query, @Mocked MessagingLock messagingLock, String messageId, int sendAttempts, int sendAttemptsMax, Date date) {
        new Expectations() {{


            entityManager.createNativeQuery(MessagingLockDaoImpl.LOCK_QUERY_SKIP_LOCKED_ORACLE, MessagingLock.class);
            result = query;

            query.getSingleResult();
            result=messagingLock;

            messagingLock.getMessageId();
            result=messageId;

            messagingLock.getSendAttempts();
            result = sendAttempts;

            messagingLock.getSendAttemptsMax();
            result = sendAttemptsMax;

            messagingLock.getStaled();
            result = date;

        }};
    }
}