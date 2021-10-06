package eu.domibus.core.earchive.job;

import eu.domibus.AbstractIT;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT;

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
    private String messageId;

    private boolean jmsManagerTriggered = false;

    @Before
    public void setUp() throws Exception {
        messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        domain = new Domain("default", "default");
        uploadPmode(SERVICE_PORT);

        String filename = "SOAPMessage2.xml";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

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

        eArchiveBatchDispatcherService.startBatch(domain);
        Assert.assertTrue(jmsManagerTriggered);

        Assert.assertEquals(1, em.createQuery("select batch from EArchiveBatchEntity batch").getResultList().size());
        Assert.assertEquals(1, em.createQuery("select batchMessage from EArchiveBatchUserMessage batchMessage").getResultList().size());
    }
}
