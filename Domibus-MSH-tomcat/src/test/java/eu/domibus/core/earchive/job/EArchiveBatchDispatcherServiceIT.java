package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
public class EArchiveBatchDispatcherServiceIT extends AbstractIT {

    JMSManager jmsManager;

    @Autowired
    EArchiveBatchDispatcherService eArchiveBatchDispatcherService;

    @Autowired
    protected Provider<SOAPMessage> mshWebserviceTest;

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
    private String messageId1;
    private String messageId2;

    @Before
    public void setUp() throws Exception {
        domain = new Domain("default", "default");
        uploadPmode(SERVICE_PORT);

        messageId1 = UUID.randomUUID().toString();
        mshWebserviceTest.invoke(soapSampleUtil.createSOAPMessage("SOAPMessage2.xml", messageId1));
        mshWebserviceTest.invoke(soapSampleUtil.createSOAPMessage("SOAPMessage2.xml", messageId2));

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
    @Transactional
    public void startBatch() {
        ReflectionTestUtils.setField(eArchiveBatchDispatcherService, "jmsManager", jmsManager);
        int initBatchSize = em.createQuery("select batch from EArchiveBatchEntity batch").getResultList().size();
        int initMessageSize =  em.createQuery("select batchMessage from EArchiveBatchUserMessage batchMessage").getResultList().size();
        eArchiveBatchDispatcherService.startBatch(domain, EArchiveRequestType.CONTINUOUS);
        Assert.assertTrue(jmsManagerTriggered);

        jmsManagerTriggered=false;

        int i = em.createQuery("update UserMessageLog u set u.archived = null")
                .executeUpdate();
        //All UserMessageLog are now available for archiving again
        eArchiveBatchDispatcherService.startBatch(domain, EArchiveRequestType.SANITIZER);
        Assert.assertTrue(jmsManagerTriggered);

        //Only 1 new batch created because START_DATE of continuous forbid the sanitizer to pick up the last message
        Assert.assertEquals(3+initBatchSize, em.createQuery("select batch from EArchiveBatchEntity batch").getResultList().size());
        Assert.assertEquals(3+initMessageSize, em.createQuery("select batchMessage from EArchiveBatchUserMessage batchMessage").getResultList().size());
    }
}