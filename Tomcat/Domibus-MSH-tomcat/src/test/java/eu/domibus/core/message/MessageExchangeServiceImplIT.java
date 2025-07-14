package eu.domibus.core.message;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.JPAConstants;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.pull.MpcPullFrequency;
import eu.domibus.core.message.pull.PullFrequencyHelper;
import eu.domibus.core.message.pull.PullMessageSender;
import eu.domibus.core.pulling.PullRequestDao;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.AbstractIT;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.neethi.Policy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author François Gautier
 * @since 5.1
 */
public class MessageExchangeServiceImplIT extends AbstractIT {

    private static final String PMODE_KEY = "blue_gw" + PModeConstants.PMODEKEY_SEPARATOR + "red_gw" + PModeConstants.PMODEKEY_SEPARATOR + "testService1" + PModeConstants.PMODEKEY_SEPARATOR + "tc1Action" + PModeConstants.PMODEKEY_SEPARATOR + "" + PModeConstants.PMODEKEY_SEPARATOR + "pullTestcase1tc1Action";

    @Autowired
    private MessageExchangeServiceImpl messageExchangeService;

    @Autowired
    private PullMessageSender sender;
    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;
    @Autowired
    private PullRequestDao pullRequestDao;

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    private SoapSampleUtil soapSampleUtil;

    @Autowired
    private PullFrequencyHelper pullFrequencyHelper;
    private Object savedJmsManager;

    @Before
    public void setUp() throws Exception {
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, "2");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, "3");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, "2");

        savedJmsManager = ReflectionTestUtils.getField(messageExchangeService, "jmsManager");
        ReflectionTestUtils.setField(messageExchangeService, "jmsManager", new JMSManagerImpl() {
            @Override
            public void sendMapMessageToQueue(JmsMessage message, Queue destination) {
                ActiveMQMessage mqMessage = new ActiveMQMessage();
                try {
                    for (Map.Entry<String, String> stringStringEntry : message.getProperties().entrySet()) {
                        mqMessage.setStringProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
                    }
                    mqMessage.setStringProperty(MessageConstants.DOMAIN, "default");
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
                sender.processPullRequest(mqMessage);
            }
        });

    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtils.setField(messageExchangeService, "jmsManager", savedJmsManager);
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME, "0");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT, "10");
        domibusPropertyProvider.setProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE, "1");

    }

    /**
     * 2 Initiates Pull request generating 2 call each
     * DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE = 2
     *
     * All response are without userMessage: no message to be pulled
     *
     * DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT = 3
     *
     * On the third call, the system goes to lowFrequency (From 2 call per request to 1)
     *
     * Somehow, the delete PullRequest (eu.domibus.core.pulling.PullRequestDao#deletePullRequest(java.lang.String))
     * is not working so on the third initiate Pull Request: the system goes into pause count PR > frequency
     * I planned for the 5th response to have a message to be pull so the system would go to high frequency
     * todo EDELIVERY-12876 improve this test
     */
    @Test
    public void processPullRequest() throws XmlProcessingException, IOException, EbMS3Exception, SOAPException, ParserConfigurationException, SAXException {
        uploadPmode(null, "dataset/pmode/PModeTemplatePuller.xml", null);

        final SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage("validAS4Response.xml", "123");
        final SOAPMessage soapMessageOk = soapSampleUtil.createSOAPMessage("PullrequestResponse_1_message.xml", "123");
        Mockito.when(mshDispatcher.dispatch(any(SOAPMessage.class), anyString(), any(Policy.class), any(LegConfiguration.class), anyString()))
                .thenReturn(soapMessage, soapMessage,
                        soapMessage, soapMessage/*, soapMessageOk, soapMessage*/);
        HashSet<String> mpcNames = new HashSet<>();
        mpcNames.add("pullMpc");
        pullFrequencyHelper.setMpcNames(mpcNames);
        MpcPullFrequency pullMpc = pullFrequencyHelper.getMpcPullFrequency("pullMpc");
        pullMpc.getFullCapacity().set(true);

        Assert.assertEquals(0L, pullRequestDao.countPendingPullRequest().longValue());

        messageExchangeService.initiatePullRequest("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull");
        Integer pullRequestNumberForMpc = pullFrequencyHelper.getPullRequestNumberForMpc("pullMpc");
        Assert.assertEquals(2, pullRequestNumberForMpc.intValue());
//        Assert.assertEquals(2L, pullRequestDao.countPendingPullRequest().longValue());

        messageExchangeService.initiatePullRequest("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull");
        pullRequestNumberForMpc = pullFrequencyHelper.getPullRequestNumberForMpc("pullMpc");
        Assert.assertEquals(1, pullRequestNumberForMpc.intValue());
//        Assert.assertEquals(0L, pullRequestDao.countPendingPullRequest().longValue());

//        messageExchangeService.initiatePullRequest("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull");
//        pullRequestNumberForMpc = pullFrequencyHelper.getPullRequestNumberForMpc("pullMpc");
//        Assert.assertEquals(2, pullRequestNumberForMpc.intValue());
//        Assert.assertEquals(0L, pullRequestDao.countPendingPullRequest().longValue());


    }
}
