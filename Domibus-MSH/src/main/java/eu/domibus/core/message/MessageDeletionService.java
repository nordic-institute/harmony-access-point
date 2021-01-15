package eu.domibus.core.message;

import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.core.message.attempt.MessageAttemptDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.*;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class MessageDeletionService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageDeletionService.class);

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private MessageInfoDao messageInfoDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private MessageAttemptDao messageAttemptDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private UIMessageDao uiMessageDao;

    @Autowired
    private MessageAcknowledgementDao messageAcknowledgementDao;

    @PersistenceContext(unitName = "domibusJTA")
    protected EntityManager em;

    @Transactional(propagation = Propagation.REQUIRED, timeout = 120)
    @Timer(clazz = DatabaseMessageHandler.class, value = "deleteMessages_oneBatch")
    @Counter(clazz = DatabaseMessageHandler.class, value = "deleteMessages_oneBatch")
    public void deleteMessages(List<UserMessageLogDto> userMessageLogs) {

        List<String> userMessageIds = userMessageLogs.stream().map(userMessageLog -> userMessageLog.getMessageId()).collect(Collectors.toList());

        LOG.info("Deleting [{}] user messages", userMessageIds.size());
        LOG.trace("Deleting user messages [{}]", userMessageIds);

        LOG.warn("deleteMessages");
        List<String> filenames = messagingDao.findFileSystemPayloadFilenames(userMessageIds);
        messagingDao.deletePayloadFiles(filenames);

        LOG.info("messageInfoDao.findSignalMessageIds");
        List<String> signalMessageIds = messageInfoDao.findSignalMessageIds(userMessageIds);
        LOG.debug("Deleting [{}] signal messages", signalMessageIds.size());
        LOG.trace("Deleting signal messages [{}]", signalMessageIds);
        LOG.info("signalMessageDao.findReceiptIdsByMessageIds");
        List<Long> receiptIds = signalMessageDao.findReceiptIdsByMessageIds(signalMessageIds);
        LOG.info("messageInfoDao.deleteMessages user");
        int deleteResult = messageInfoDao.deleteMessages(userMessageIds);
        LOG.debug("Deleted [{}] messageInfo for userMessage.", deleteResult);
        LOG.info("messageInfoDao.deleteMessages signal");
        deleteResult = messageInfoDao.deleteMessages(signalMessageIds);
        LOG.debug("Deleted [{}] messageInfo for signalMessage.", deleteResult);
        LOG.info("signalMessageDao.deleteReceipts");
        deleteResult = signalMessageDao.deleteReceipts(receiptIds);
        LOG.debug("Deleted [{}] receipts.", deleteResult);
        LOG.info("userMessageLogDao.deleteMessageLogs user");
        deleteResult = userMessageLogDao.deleteMessageLogs(userMessageIds);
        LOG.debug("Deleted [{}] userMessageLogs.", deleteResult);
        LOG.info("signalMessageLogDao.deleteMessageLogs signal");
        deleteResult = signalMessageLogDao.deleteMessageLogs(signalMessageIds);
        LOG.debug("Deleted [{}] signalMessageLogs.", deleteResult);
        deleteResult = messageAttemptDao.deleteAttemptsByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] messageSendAttempts.", deleteResult);
        deleteResult = errorLogDao.deleteErrorLogsByMessageIdInError(userMessageIds);
        LOG.debug("Deleted [{}] deleteErrorLogsByMessageIdInError.", deleteResult);
        deleteResult = uiMessageDao.deleteUIMessagesByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] deleteUIMessagesByMessageIds for userMessages.", deleteResult);
        deleteResult = uiMessageDao.deleteUIMessagesByMessageIds(signalMessageIds);
        LOG.debug("Deleted [{}] deleteUIMessagesByMessageIds for signalMessages.", deleteResult);
        deleteResult = messageAcknowledgementDao.deleteMessageAcknowledgementsByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] deleteMessageAcknowledgementsByMessageIds.", deleteResult);

        LOG.warn("end deleteMessages");

       // backendNotificationService.notifyMessageDeleted(userMessageLogs);
    }
    public void execute() {

        Date startDate = new Date(System.currentTimeMillis());
        String mpc = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";
        Integer expiredDownloadedMessagesLimit = 10000;


        StatelessSession statelessSession = null;
        Transaction txn = null;
        ScrollableResults scrollableResults = null;
        try {
            SessionFactory sessionFactory = ((Session) em.getDelegate()).getSessionFactory();
            statelessSession = sessionFactory.openStatelessSession();
            //statelessSession.setJdbcBatchSize(100);
            txn = statelessSession.getTransaction();
            long start = System.currentTimeMillis();
            txn.begin();
            Query query = statelessSession
                    .createNamedQuery("UserMessageLog.findSentUserMessagesOlderThan");
            query.setParameter("DATE", startDate);
            query.setParameter("MPC", mpc);
            query.setMaxResults(expiredDownloadedMessagesLimit);
            query.setFetchSize(1000);
            query.setReadOnly(true);
            query.setLockMode("a", LockMode.NONE);
            ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
            //Query deleteQuery = statelessSession.createQuery("delete from MessageInfo mi where mi.messageId=:MESSAGEID");

            final javax.persistence.Query delQuery = em.createQuery("delete from MessageInfo mi where mi.messageId=:MESSAGEID");

            while (results.next()) {

                delQuery.setParameter( "MESSAGEID", ((MessageDto)results.get(0)).getUserMessageId() )
                .executeUpdate();
            }
            txn.commit();
            LOG.info("Execution time: [{}]", System.currentTimeMillis()-start);


        } catch (RuntimeException e) {
            if (txn != null && txn.getStatus() == TransactionStatus.ACTIVE) txn.rollback();
            throw e;
        } finally {
            if (scrollableResults != null) {
                scrollableResults.close();
            }
            if (statelessSession != null) {
                statelessSession.close();
            }
        }
    }

}
