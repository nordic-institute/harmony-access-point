package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.earchive.EArchiveBatchUserMessageDao;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
@Ignore("EDELIVERY-8927: Bamboo - Sonar Branch plan is failing due to IT test failures")
public class EArchiveBatchDispatcherServiceIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchDispatcherServiceIT.class);

    JMSManager jmsManager;

    @Autowired
    EArchiveBatchDispatcherService eArchiveBatchDispatcherService;

    @Autowired
    protected MSHWebservice mshWebserviceTest;

    @Autowired
    protected SoapSampleUtil soapSampleUtil;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    private Domain domain;

    private boolean jmsManagerTriggered = false;
    private int userMessageFound = 0;
    private String messageId1;

    @Autowired
    private UserMessageLogDao userMessageLogDao;
    @Autowired
    private EArchiveBatchDao userMessageDao;
    @Autowired
    private EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    @Before
    public void setUp() throws Exception {
        domain = new Domain("default", "default");
        uploadPmode(SERVICE_PORT);

        messageId1 = UUID.randomUUID().toString();
        mshWebserviceTest.invoke(soapSampleUtil.createSOAPMessage("SOAPMessage4.xml", messageId1));

        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_ACTIVE, "true");
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_ALERT_EARCHIVING_MSG_NON_FINAL_ACTIVE, "false");
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_BATCH_SIZE, "1");
        domibusPropertyProvider.setProperty(DomainService.DEFAULT_DOMAIN, DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT, "0");
        jmsManager = new JMSManagerImpl() {
            public void sendMessageToQueue(JmsMessage message, Queue destination) {
                jmsManagerTriggered = true;
            }
        };
    }

    @Test
    public void startBatch() {
        ReflectionTestUtils.setField(eArchiveBatchDispatcherService, "jmsManager", jmsManager);
        eArchiveBatchDispatcherService.startBatch(domain, EArchiveRequestType.CONTINUOUS);
        Assert.assertTrue(jmsManagerTriggered);

        jmsManagerTriggered = false;

        UserMessageLog byMessageId = userMessageLogDao.findByMessageId(messageId1);
        byMessageId.setArchived(null);
        //All UserMessageLog are now available for archiving again
        eArchiveBatchDispatcherService.startBatch(domain, EArchiveRequestType.SANITIZER);
        //Only 1 new batch created because START_DATE of continuous forbid the sanitizer to pick up the last message
        List<EArchiveBatchEntity> batchesByStatus = userMessageDao.findBatchesByStatus(Arrays.asList(EArchiveBatchStatus.values()), 10000);
        for (EArchiveBatchEntity byStatus : batchesByStatus) {
            List<EArchiveBatchUserMessage> batchMessageList = eArchiveBatchUserMessageDao.getBatchMessageList(byStatus.getBatchId(), 0, 1000);
            for (EArchiveBatchUserMessage eArchiveBatchUserMessage : batchMessageList) {
                if (StringUtils.equals(eArchiveBatchUserMessage.getMessageId(), messageId1)) {
                    LOG.info(eArchiveBatchUserMessage.toString());
                    userMessageFound++;
                }
            }
        }
        Assert.assertEquals(1, userMessageFound);
    }
}