package eu.domibus.core.message;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.message.pull.*;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.test.util.PojoInstaciatorUtil;
import org.apache.commons.lang3.Validate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageExchangeServiceImplTest {

    @Mock
    private PModeProvider pModeProvider;

    @Mock
    private ConfigurationDAO configurationDao;

    @Mock
    private JMSManager jmsManager;

    @Mock
    private EbMS3MessageBuilder messageBuilder;

    @Mock
    private MessagingDao messagingDao;

    @Mock
    private UserMessageLogDao userMessageLogDao;

    @Mock
    private PullMessageService pullMessageService;

    @Mock
    private DomibusPropertyProvider domibusPropertyProvider;

    @Mock
    private DomibusConfigurationService domibusConfigurationService;

    @Mock
    private DomainContextProvider domainProvider;

    @Mock
    MpcService mpcService;

    @Spy
    private ProcessValidator processValidator;

    @Mock
    private PullFrequencyHelper pullFrequencyHelper;

    @InjectMocks
    private MessageExchangeServiceImpl messageExchangeService;

    private Process process;

    private Party correctParty;

    @Before
    public void init() {
        correctParty = new Party();
        correctParty.setName("party1");
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:responder]}", "initiatorParties{[name:initiator]}");

        Service service = new Service();
        service.setName("service1");
        findLegByName("leg1").setService(service);

        service = new Service();
        service.setName("service2");
        findLegByName("leg2").setService(service);

        when(pModeProvider.getGatewayParty()).thenReturn(correctParty);
        when(configurationDao.configurationExists()).thenReturn(true);
        List<Process> processes = Lists.newArrayList(process);
        when(pModeProvider.findPullProcessesByInitiator(correctParty)).thenReturn(processes);
        Mockito.doNothing().when(processValidator).validatePullProcess(Matchers.any(List.class));
    }

    private LegConfiguration findLegByName(final String name) {
        final Set<LegConfiguration> filter = Sets.filter(process.getLegs(), new Predicate<LegConfiguration>() {
            @Override
            public boolean apply(LegConfiguration legConfiguration) {
                return name.equals(legConfiguration.getName());
            }
        });
        Validate.isTrue(filter.size() == 1);
        return filter.iterator().next();
    }

    @Test
    public void testGetPartyId() throws Exception {
        String mpc = "mpcValue";
        String expectedPartyId = "BE1234567890";
        String party1 = "party1";

        when(pullMessageService.allowDynamicInitiatorInPullProcess()).thenReturn(true);
        when(mpcService.extractInitiator(mpc)).thenReturn(expectedPartyId);

        Set<String> partyIds = messageExchangeService.getPartyIds(mpc, new Party());
        assertTrue(partyIds.contains(expectedPartyId));

        Party party = Mockito.mock(Party.class);
        when(party.getIdentifiers()).thenReturn(null);

        partyIds = messageExchangeService.getPartyIds(mpc, party);
        assertTrue(partyIds.contains(expectedPartyId));


        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setPartyId(party1);
        identifiers.add(identifier);
        when(party.getIdentifiers()).thenReturn(identifiers);

        partyIds = messageExchangeService.getPartyIds(null, party);
        assertTrue(partyIds.contains(party1));
    }

    @Test
    public void testSuccessFullOneWayPullConfiguration() throws Exception {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "initiatorParties{[name:resp1]}");
        MessageStatus messageStatus = getMessageStatus(process);
        assertEquals(MessageStatus.READY_TO_PULL, messageStatus);
    }


    private MessageStatus getMessageStatus(Process process) throws EbMS3Exception {
        List<Process> processes = Lists.newArrayList();
        processes.add(process);
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        when(pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        return messageExchangeService.getMessageStatus(messageExchangeConfiguration);
    }

    @Test(expected = PModeException.class)
    public void testIncorrectMultipleProcessFoundForConfiguration() throws EbMS3Exception {
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agreementName", "senderParty", "receiverParty", "service", "action", "leg");
        List<Process> processes = Lists.newArrayList();
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]");
        processes.add(process);
        process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:push]");
        processes.add(process);
        when(pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(processes);
        doThrow(new PModeException(DomibusCoreErrorCode.DOM_003, "pMode exception")).when(processValidator).validatePullProcess(Matchers.any(List.class));
        messageExchangeService.getMessageStatus(messageExchangeConfiguration);
    }

    @Test
    public void testInitiatePullRequest() {
        when(pModeProvider.isConfigurationLoaded()).thenReturn(true);
        when(domainProvider.getCurrentDomain()).thenReturn(new Domain("default", "Default"));
        when(pullFrequencyHelper.getTotalPullRequestNumberPerJobCycle()).thenReturn(25);
        when(pullFrequencyHelper.getPullRequestNumberForMpc("test1")).thenReturn(10);
        when(pullFrequencyHelper.getPullRequestNumberForMpc("test2")).thenReturn(15);
        when(jmsManager.getDestinationSize("pull")).thenReturn(20l);

        ArgumentCaptor<JmsMessage> mapArgumentCaptor = ArgumentCaptor.forClass(JmsMessage.class);
        messageExchangeService.initiatePullRequest();
        verify(pModeProvider, times(1)).getGatewayParty();
        verify(jmsManager, times(25)).sendMapMessageToQueue(mapArgumentCaptor.capture(), any(Queue.class));
        String pModeKeyResult = "party1" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "responder" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "service1" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "Mock" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "Mock" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR + "leg1";

        TestResult testResult = new TestResult("qn1", pModeKeyResult, "false");
        pModeKeyResult = "party1" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "responder" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "service2" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "Mock" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR +
                "Mock" + MessageExchangeConfiguration.PMODEKEY_SEPARATOR + "leg2";

        testResult.chain(new TestResult("qn2", pModeKeyResult, "false"));
        final List<JmsMessage> allValues = mapArgumentCaptor.getAllValues();
        for (JmsMessage allValue : allValues) {
            assertTrue(testResult.testSucced(allValue.getProperties()));
        }
    }

    @Test
    public void testInitiatePullRequestWithoutConfiguration() throws Exception {
        when(pModeProvider.isConfigurationLoaded()).thenReturn(false);
        messageExchangeService.initiatePullRequest();
        verify(pModeProvider, times(0)).findPullProcessesByInitiator(any(Party.class));
    }


    @Test
    public void testInvalidRequest() throws Exception {
        when(messageBuilder.buildSOAPMessage(any(SignalMessage.class), any(LegConfiguration.class))).thenThrow(
                new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "An error occurred while processing your request. Please check the message header for more details.", null, null));
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "initiatorParties{[name:initiator]}");

        List<Process> processes = Lists.newArrayList(process);
        when(pModeProvider.findPullProcessesByInitiator(correctParty)).thenReturn(processes);
        when(pModeProvider.findPullProcessesByMessageContext(any(MessageExchangeConfiguration.class))).thenReturn(Lists.newArrayList(process));
        messageExchangeService.initiatePullRequest();
        verify(jmsManager, times(0)).sendMessageToQueue(any(JmsMessage.class), any(Queue.class));
    }

    @Test
    public void extractProcessOnMpc() throws Exception {
        List<Process> processes = Lists.newArrayList(PojoInstaciatorUtil.instanciate(Process.class, "mep[name:oneway]", "mepBinding[name:pull]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "initiatorParties{[name:resp1]}"));
        when(pModeProvider.findPullProcessByMpc(eq("qn1"))).thenReturn(processes);
        PullContext pullContext = messageExchangeService.extractProcessOnMpc("qn1");
        assertEquals("resp1", pullContext.getInitiator().getName());
        assertEquals("party1", pullContext.getResponder().getName());
        assertEquals("oneway", pullContext.getProcess().getMep().getName());
    }

    @Test(expected = PModeException.class)
    public void extractProcessMpcWithNoProcess() throws Exception {
        when(pModeProvider.findPullProcessByMpc(eq("qn1"))).thenReturn(new ArrayList<Process>());
        doThrow(new PModeException(DomibusCoreErrorCode.DOM_003, "pMode exception")).when(processValidator).validatePullProcess(Matchers.any(List.class));
        messageExchangeService.extractProcessOnMpc("qn1");
    }

    @Test(expected = PModeException.class)
    public void extractProcessMpcWithNoToManyProcess() throws Exception {
        when(pModeProvider.findPullProcessByMpc(eq("qn1"))).thenReturn(Lists.newArrayList(new Process(), new Process()));
        doThrow(new PModeException(DomibusCoreErrorCode.DOM_003, "pMode exception")).when(processValidator).validatePullProcess(Matchers.any(List.class));
        messageExchangeService.extractProcessOnMpc("qn1");
    }

    @Test
    public void testRetrieveReadyToPullUserMessageIdWithNoMessage() {
        String mpc = "mpc";
        Party party = Mockito.mock(Party.class);

        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setPartyId("party1");
        identifiers.add(identifier);

        when(party.getIdentifiers()).thenReturn(identifiers);

        when(messagingDao.findMessagingOnStatusReceiverAndMpc(eq("party1"), eq(MessageStatus.READY_TO_PULL), eq(mpc))).thenReturn(Lists.<MessagePullDto>newArrayList());

        final String messageId = messageExchangeService.retrieveReadyToPullUserMessageId(mpc, party);
        assertNull(messageId);

    }

    @Test
    public void testRetrieveReadyToPullUserMessageIdWithMessage() {
        String mpc = "mpc";
        Party party = Mockito.mock(Party.class);

        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setPartyId("party1");
        identifiers.add(identifier);

        when(party.getIdentifiers()).thenReturn(identifiers);

        final String testMessageId = "testMessageId";
        when(pullMessageService.getPullMessageId(eq("party1"), eq(mpc))).thenReturn(testMessageId);
        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(MessageStatus.READY_TO_PULL);
        when(userMessageLogDao.findByMessageId(testMessageId)).thenReturn(userMessageLog);

        final String messageId = messageExchangeService.retrieveReadyToPullUserMessageId(mpc, party);
        assertEquals(testMessageId, messageId);

    }

    @Test
    public void testRetrieveReadyToPullUserMessageIdWithMessageWithEmptyIdentifier() {
        String mpc = "mpc";
        Party party = Mockito.mock(Party.class);

        List<Identifier> identifiers = new ArrayList<>();
        when(party.getIdentifiers()).thenReturn(identifiers);

        assertNull(messageExchangeService.retrieveReadyToPullUserMessageId(mpc, party));

    }

    @Test
    public void testRetrieveReadyToPullUserMessageINoMessage() {
        String mpc = "mpc";
        Party party = Mockito.mock(Party.class);

        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setPartyId("party1");
        identifiers.add(identifier);

        when(party.getIdentifiers()).thenReturn(identifiers);

        when(pullMessageService.getPullMessageId(eq("party1"), eq(mpc))).thenReturn(null);
        assertNull(messageExchangeService.retrieveReadyToPullUserMessageId(mpc, party));

    }


    @Test
    public void testGetMessageStatusWhenNoPullProcessFound() {
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agr1",
                "sender",
                "receiver",
                "serv1",
                "action1",
                "leg1");
        when(pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(Lists.<Process>newArrayList());
        final MessageStatus messageStatus = messageExchangeService.getMessageStatus(messageExchangeConfiguration);
        assertEquals(MessageStatus.SEND_ENQUEUED, messageStatus);

    }

    @Test
    public void testRetrieveMessageRestoreStatusWithValidPull() throws EbMS3Exception {
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agr1",
                "sender",
                "receiver",
                "serv1",
                "action1",
                "leg1");
        UserMessage userMessage = new UserMessage();
        userMessage.setMpc("mpc123");
        when(messagingDao.findUserMessageByMessageId("123")).thenReturn(userMessage);
        when(pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING)).thenReturn(messageExchangeConfiguration);
        when(pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration)).thenReturn(Lists.newArrayList(process));
        final MessageStatus messageStatus = messageExchangeService.retrieveMessageRestoreStatus("123");
        assertEquals(MessageStatus.READY_TO_PULL, messageStatus);

    }

    @Test
    public void testRetrieveMessageRestoreStatusWithForcePull() throws EbMS3Exception {
        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("agr1",
                "sender",
                "receiver",
                "serv1",
                "action1",
                "leg1");
        UserMessage userMessage = new UserMessage();
        userMessage.setMpc("mpc123");
        when(messagingDao.findUserMessageByMessageId("123")).thenReturn(userMessage);
        when(mpcService.forcePullOnMpc(userMessage)).thenReturn(true);
        final MessageStatus messageStatus = messageExchangeService.retrieveMessageRestoreStatus("123");
        assertEquals(MessageStatus.READY_TO_PULL, messageStatus);
    }



}