package eu.domibus.core.pmode.provider;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.pmode.*;
import eu.domibus.core.pmode.validation.PModeValidationService;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.MessageExchangePattern;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.test.util.PojoInstaciatorUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Arun Raj, Soumya Chandran
 * @since 3.3
 */
@RunWith(JMockit.class)
public class CachingPModeProviderTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CachingPModeProviderTest.class);

    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
    private static final String VALID_PMODE_TEST_CONFIG_URI = "samplePModes/domibus-configuration-valid-testservice.xml";
    private static final String PULL_PMODE_CONFIG_URI = "samplePModes/domibus-pmode-with-pull-processes.xml";
    private static final String DEFAULT_MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMpc";
    private static final String ANOTHER_MPC_URI = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/anotherMpc";
    private static final String DEFAULTMPC = "defaultMpc";
    private static final String ANOTHERMPC = "anotherMpc";
    private static final String NONEXISTANTMPC = "NonExistantMpc";

    @Injectable
    ConfigurationDAO configurationDAO;

    @Injectable
    ConfigurationRawDAO configurationRawDAO;

    @Injectable
    EntityManager entityManager;

    @Injectable
    JAXBContext jaxbContextConfig;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    PModeValidationService pModeValidationService;

    @Injectable
    Configuration configuration;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    ProcessPartyExtractorProvider processPartyExtractorProvider;

    @Injectable
    Topic clusterCommandTopic;

    @Injectable
    Domain domain = DomainService.DEFAULT_DOMAIN;

    @Injectable
    SignalService signalService;

    @Injectable
    private MpcService mpcService;

    @Injectable
    PullMessageService pullMessageService;

    @Tested
    CachingPModeProvider cachingPModeProvider;


    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist", null);
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

    @Test
    public void testIsMpcExistant() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);

        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(Boolean.TRUE, cachingPModeProvider.isMpcExistant(DEFAULTMPC.toUpperCase()));
    }

    @Test
    public void testIsMpcExistantNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(Boolean.FALSE, cachingPModeProvider.isMpcExistant(NONEXISTANTMPC));
    }

    @Test
    public void testGetRetentionDownloadedByMpcName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(3, cachingPModeProvider.getRetentionDownloadedByMpcName(ANOTHERMPC.toLowerCase()));
    }

    @Test
    public void testGetRetentionDownloadedByMpcNameNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(0, cachingPModeProvider.getRetentionDownloadedByMpcName(NONEXISTANTMPC));
    }


    @Test
    public void testGetRetentionUnDownloadedByMpcName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(5, cachingPModeProvider.getRetentionUndownloadedByMpcName(ANOTHERMPC.toUpperCase()));
    }

    @Test
    public void testGetRetentionUnDownloadedByMpcNameNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(-1, cachingPModeProvider.getRetentionUndownloadedByMpcName(NONEXISTANTMPC));
    }

    @Test
    public void testGetMpcURIList() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        List<String> result = cachingPModeProvider.getMpcURIList();
        Assert.assertTrue("URI list should contain DefaultMpc URI", result.contains(DEFAULT_MPC_URI));
        Assert.assertTrue("URI list should contain AnotherMpc URI", result.contains(ANOTHER_MPC_URI));
    }

    @Test
    public void testFindPartyName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            result = configuration.getBusinessProcesses().getParties();
        }};

        Collection<PartyId> partyIdCollection = new ArrayList<>();
        //TODO Use Mocking instead of real Instances
        PartyId partyId1 = new PartyId();
        partyId1.setValue("EmptyTestParty");
        partyId1.setType("ABC><123");
        partyIdCollection.add(partyId1);

        try {
            cachingPModeProvider.findPartyName(partyIdCollection);
            Assert.fail("Expected EbMS3Exception due to invalid URI character present!!");
        } catch (Exception e) {
            Assert.assertTrue("Expected EbMS3Exception", e instanceof EbMS3Exception);
        }
    }

    @Test
    public void testRefresh() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.configurationDAO.configurationExists();
            result = true;

            cachingPModeProvider.configurationDAO.readEager();
            result = configuration;
        }};

        cachingPModeProvider.refresh();
        Assert.assertEquals(configuration, cachingPModeProvider.getConfiguration());
    }

    @Test
    public void testGetBusinessProcessRoleOk() throws Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getRoles();
            result = configuration.getBusinessProcesses().getRoles();
        }};
        //TODO Use Mocking instead of real Instances
        Role expectedRole = new Role();
        expectedRole.setName("defaultInitiatorRole");
        expectedRole.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

        Role role = cachingPModeProvider.getBusinessProcessRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        Assert.assertEquals(expectedRole, role);
    }

    @Test
    public void testGetBusinessProcessRoleNOk() throws Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getRoles();
            result = configuration.getBusinessProcesses().getRoles();
        }};

        Role role = cachingPModeProvider.getBusinessProcessRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator123");
        Assert.assertNull(role);
    }

    @Test
    public void testRetrievePullProcessBasedOnInitiator() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        final Set<Party> parties = new HashSet<>(configuration.getBusinessProcesses().getParties());
        final Party red_gw = getPartyByName(parties, "red_gw");
        final Party blue_gw = getPartyByName(parties, "blue_gw");
        final Set<Process> processes = new HashSet<>(configuration.getBusinessProcesses().getProcesses());
        final Collection<Process> filter = Collections2.filter(processes, new Predicate<Process>() {
            @Override
            public boolean apply(Process process) {
                return process.getInitiatorParties().contains(red_gw) && "pull".equals(process.getMepBinding().getName());
            }
        });
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
            configurationDAO.readEager();
            result = configuration;
        }};
        cachingPModeProvider.init();

        List<Process> pullProcessesByInitiator = cachingPModeProvider.findPullProcessesByInitiator(red_gw);
        Assert.assertEquals(5, pullProcessesByInitiator.size());

        pullProcessesByInitiator = cachingPModeProvider.findPullProcessesByInitiator(blue_gw);
        Assert.assertEquals(0, pullProcessesByInitiator.size());
    }

    @Test
    public void testRetrievePullProcessBasedOnPartyNotInInitiator() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        final Set<Party> parties = new HashSet<>(configuration.getBusinessProcesses().getParties());
        final Party white_gw = getPartyByName(parties, "white_gw");
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
            configurationDAO.readEager();
            result = configuration;
        }};
        cachingPModeProvider.init();
        List<Process> pullProcessesByInitiator = cachingPModeProvider.findPullProcessesByInitiator(white_gw);
        Assert.assertNotNull(pullProcessesByInitiator);
    }

    @Test
    public void testRetrievePullProcessBasedOnMpc() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        final String mpcName = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPCOne";
        final String emptyMpc = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPCTwo";
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
            configurationDAO.readEager();
            result = configuration;
        }};
        cachingPModeProvider.init();
        List<Process> pullProcessesByMpc = cachingPModeProvider.findPullProcessByMpc(mpcName);
        Assert.assertEquals(1, pullProcessesByMpc.size());
        Assert.assertEquals(pullProcessesByMpc.iterator().next().getName(), "tc13Process");
        pullProcessesByMpc = cachingPModeProvider.findPullProcessByMpc(emptyMpc);
        Assert.assertEquals(1, pullProcessesByMpc.size());
        Assert.assertEquals(pullProcessesByMpc.iterator().next().getName(), "tc14Process");


    }

    @Test
    public void testFindPartyIdByServiceAndAction() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        List<String> expectedList = new ArrayList<>();
        expectedList.add("domibus-blue");
        expectedList.add("domibus-red");
        expectedList.add("urn:oasis:names:tc:ebcore:partyid-type:unregistered:holodeck-b2b");
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
        }};

        // When
        List<String> partyIdByServiceAndAction = cachingPModeProvider.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, null);

        // Then
        Assert.assertEquals(expectedList, partyIdByServiceAndAction);
    }

    @Test
    public void testFindPushToPartyIdByServiceAndAction() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        List<String> expectedList = new ArrayList<>();
        expectedList.add("domibus-blue");
        expectedList.add("domibus-red");
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
        }};

        List<MessageExchangePattern> meps = new ArrayList<>();
        meps.add(MessageExchangePattern.ONE_WAY_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PULL);
        meps.add(MessageExchangePattern.TWO_WAY_PULL_PUSH);
        // When
        List<String> partyIdByServiceAndAction = cachingPModeProvider.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, meps);

        // Then
        Assert.assertEquals(expectedList, partyIdByServiceAndAction);
    }


    private Party getPartyByName(Set<Party> parties, final String partyName) {
        final Collection<Party> filter = Collections2.filter(parties, new Predicate<Party>() {
            @Override
            public boolean apply(Party party) {
                return partyName.equals(party.getName());
            }
        });
        return Lists.newArrayList(filter).get(0);
    }

    @Test
    public void testGetPartyIdType() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        String partyIdentifier = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-de";
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            result = configuration.getBusinessProcesses().getParties();
        }};

        // When
        String partyIdType = cachingPModeProvider.getPartyIdType(partyIdentifier);

        // Then
        Assert.assertTrue(StringUtils.isEmpty(partyIdType));
    }

    @Test
    public void testGetPartyIdTypeNull() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        String partyIdentifier = "empty";

        // When
        String partyIdType = cachingPModeProvider.getPartyIdType(partyIdentifier);

        // Then
        Assert.assertNull(partyIdType);
    }

    @Test
    public void testGetServiceType() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        //Given
        String serviceValue = "bdx:noprocess";
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getServices();
            result = configuration.getBusinessProcesses().getServices();
        }};

        // When
        String serviceType = cachingPModeProvider.getServiceType(serviceValue);

        // Then
        Assert.assertTrue(Sets.newHashSet("tc1", "tc2", "tc3").contains(serviceType));
    }

    @Test
    public void testGetServiceTypeNull() {
        // Given
        String serviceValue = "serviceValue";

        // When
        String serviceType = cachingPModeProvider.getServiceType(serviceValue);

        // Then
        Assert.assertNull(serviceType);
    }

    @Test
    public void testGetProcessFromService() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
        }};

        // When
        List<Process> processFromService = cachingPModeProvider.getProcessFromService(Ebms3Constants.TEST_SERVICE);

        // Then
        Assert.assertEquals(2, processFromService.size());
        Assert.assertEquals("testService", processFromService.get(0).getName());
    }

    @Test
    public void testGetProcessFromServiceNull() {
        // Given
        String serviceValue = "serviceValue";

        // When
        List<Process> processFromService = cachingPModeProvider.getProcessFromService(serviceValue);

        // Then
        Assert.assertTrue(processFromService.isEmpty());
    }

    @Test
    public void testGetRoleInitiator() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();

            cachingPModeProvider.getProcessFromService(Ebms3Constants.TEST_SERVICE);
            result = getTestProcess(configuration.getBusinessProcesses().getProcesses());

        }};

        // When
        String initiator = cachingPModeProvider.getRole("INITIATOR", Ebms3Constants.TEST_SERVICE);

        // Then
        Assert.assertEquals("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator", initiator);
    }

    @Test
    public void testGetRoleResponder() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();

            cachingPModeProvider.getProcessFromService(Ebms3Constants.TEST_SERVICE);
            result = getTestProcess(configuration.getBusinessProcesses().getProcesses());

        }};

        // When
        String responder = cachingPModeProvider.getRole("RESPONDER", Ebms3Constants.TEST_SERVICE);

        // Then
        Assert.assertEquals("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder", responder);
    }

    @Test
    public void testGetRoleNull() {
        // Given
        String serviceValue = "serviceValue";

        // When
        String role = cachingPModeProvider.getRole("", serviceValue);

        // Then
        Assert.assertNull(role);
    }

    @Test
    public void testAgreementRef() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(VALID_PMODE_TEST_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();

            cachingPModeProvider.getProcessFromService(Ebms3Constants.TEST_SERVICE);
            result = getTestProcess(configuration.getBusinessProcesses().getProcesses());

        }};

        // When
        String agreementRef = cachingPModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE);

        // Then
        Assert.assertEquals("TestServiceAgreement", agreementRef);
    }

    @Test
    public void testAgreementRefNull() {
        // Given
        String serviceValue = "serviceValue";

        // When
        String agreementRef = cachingPModeProvider.getAgreementRef(serviceValue);

        // Then
        Assert.assertNull(agreementRef);
    }

    @Test(expected = EbMS3Exception.class)
    public void testFindPullLegExeption() throws EbMS3Exception, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
        }};

        try {
            cachingPModeProvider.findPullLegName("agreementName", "senderParty", "receiverParty", "service", "action", "mpc");
        } catch (EbMS3Exception exc) {
            Assert.assertTrue(ErrorCode.EbMS3ErrorCode.EBMS_0001.equals(exc.getErrorCode()));
            throw exc;
        }
    }

    @Test
    public void testFindPullLeg() throws EbMS3Exception, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
            cachingPModeProvider.matchAgreement((Process) any, anyString);
            result = true;
            cachingPModeProvider.matchInitiator((Process) any, (ProcessTypePartyExtractor) any);
            result = true;
            cachingPModeProvider.matchResponder((Process) any, (ProcessTypePartyExtractor) any);
            result = true;
            cachingPModeProvider.candidateMatches((LegConfiguration) any, anyString, anyString, anyString);
            result = true;
        }};

        String legName = cachingPModeProvider.findPullLegName("", "somesender", "somereceiver", "someservice", "someaction", "somempc");
        Assert.assertNotNull(legName);
    }

    @Test(expected = EbMS3Exception.class)
    public void testFindPullLegNoCandidate() throws EbMS3Exception, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
            cachingPModeProvider.matchAgreement((Process) any, anyString);
            result = false;
        }};

        try {
            cachingPModeProvider.findPullLegName("", "somesender", "somereceiver", "someservice", "someaction", "somempc");
        } catch (EbMS3Exception exc) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0001, exc.getErrorCode());
            throw exc;
        }
        // exception should have been raised
        Assert.assertFalse(true);
    }

    @Test(expected = EbMS3Exception.class)
    public void testFindPullLegNoMatchingCandidate() throws EbMS3Exception, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        // Given
        configuration = loadSamplePModeConfiguration(PULL_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
            result = configuration.getBusinessProcesses().getProcesses();
            cachingPModeProvider.matchAgreement((Process) any, anyString);
            result = true;
            cachingPModeProvider.matchInitiator((Process) any, (ProcessTypePartyExtractor) any);
            result = true;
            cachingPModeProvider.matchResponder((Process) any, (ProcessTypePartyExtractor) any);
            result = true;
            cachingPModeProvider.candidateMatches((LegConfiguration) any, anyString, anyString, anyString);
            result = false;
        }};

        try {
            cachingPModeProvider.findPullLegName("", "somesender", "somereceiver", "someservice", "someaction", "somempc");
        } catch (EbMS3Exception exc) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0001, exc.getErrorCode());
            throw exc;
        }

        // exception should have been raised
        Assert.assertFalse(true);
    }

    @Test
    public void testGetGatewayParty() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);

        new Expectations() {{
            cachingPModeProvider.getConfiguration().getParty();
            result = configuration.getParty();
        }};

        Assert.assertEquals(configuration.getParty(), cachingPModeProvider.getGatewayParty());
    }


    @Test
    public void testMatchAgreement() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]", "mepBinding[name:push]", "agreement[name:a1,value:v1,type:t1]");
        Assert.assertTrue(cachingPModeProvider.matchAgreement(process, "a1"));
    }

    @Test
    public void testMatchInitiator() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "initiatorParties{[name:initiator1];[name:initiator2]}");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor(null, "initiator1");
        Assert.assertTrue(cachingPModeProvider.matchInitiator(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchInitiatorNot() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "initiatorParties{[name:initiator1];[name:initiator2]}");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor(null, "nobodywho");
        Assert.assertFalse(cachingPModeProvider.matchInitiator(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchInitiatorAllowEmpty() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        new Expectations() {{
            pullMessageService.allowDynamicInitiatorInPullProcess();
            result = true;
        }};
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor(null, "nobodywho");
        Assert.assertTrue(cachingPModeProvider.matchInitiator(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchInitiatorNotAllowEmpty() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        new Expectations() {{
            pullMessageService.allowDynamicInitiatorInPullProcess();
            result = false;
        }};
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor(null, "nobodywho");
        Assert.assertFalse(cachingPModeProvider.matchInitiator(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchResponder() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "responderParties{[name:responder1];[name:responder2]}");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor("responder1", null);
        Assert.assertTrue(cachingPModeProvider.matchResponder(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchResponderNot() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "responderParties{[name:responder1];[name:responder2]}");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor("nobody", null);
        Assert.assertFalse(cachingPModeProvider.matchResponder(process, processTypePartyExtractor));
    }

    @Test
    public void testMatchResponderEmpty() {
        Process process = PojoInstaciatorUtil.instanciate(Process.class, "mep[name:twoway]");
        ProcessTypePartyExtractor processTypePartyExtractor = new PullProcessPartyExtractor("nobody", null);
        Assert.assertFalse(cachingPModeProvider.matchResponder(process, processTypePartyExtractor));
    }

    @Test
    public void testCandidateMatches() {
        LegConfiguration candidate = PojoInstaciatorUtil.instanciate(LegConfiguration.class, "service[name:s1]", "action[name:a1]", "defaultMpc[qualifiedName:mpc_qn]");
        Assert.assertTrue(cachingPModeProvider.candidateMatches(candidate, "s1", "a1", "mpc_qn"));
    }

    @Test
    public void testCandidateNotMatches() {
        LegConfiguration candidate = PojoInstaciatorUtil.instanciate(LegConfiguration.class, "service[name:s1]", "action[name:a1]", "defaultMpc[qualifiedName:mpc_qn]");
        Assert.assertFalse(cachingPModeProvider.candidateMatches(candidate, "s2", "a2", "mpc_qn"));
    }

    @Test
    public void testfindMpcUri() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, EbMS3Exception {
        String expectedMpc = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMpc";
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        String mpcURI = cachingPModeProvider.findMpcUri("defaultMpc");

        Assert.assertEquals(expectedMpc, mpcURI);
    }

    @Test(expected = EbMS3Exception.class)
    public void testfindMpcUriException() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, EbMS3Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        cachingPModeProvider.findMpcUri("no_mpc");

        // exception is expected before this line
        Assert.assertFalse(true);
    }


    private Process getTestProcess(Collection<Process> processes) {
        for (Process process : processes) {
            if (process.getName().equals("testService")) {
                return process;
            }
        }
        return null;
    }


    @Test
    public void testgetMpcList(@Injectable Mpc mpc) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, EbMS3Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        cachingPModeProvider.getMpcList();
        Assert.assertNotNull(cachingPModeProvider.getMpcList());
    }

    @Test(expected = EbMS3Exception.class)
    public void testfindLegNameEmptyCandidate(@Injectable Party party,
                                              @Injectable Party responder,
                                              @Injectable Process process,
                                              @Injectable ProcessTypePartyExtractor processTypePartyExtractor) throws EbMS3Exception {
        String senderParty = "red_gw";
        String receiverParty = "blue_gw";
        new Expectations() {
            {
                cachingPModeProvider.getConfiguration().getBusinessProcesses().getProcesses();
                result = process;
                processPartyExtractorProvider.getProcessTypePartyExtractor(process.getMepBinding().getValue(), "red_gw", "blue_gw");
                result = processTypePartyExtractor;
                process.getInitiatorParties();
                result = party;
                party.getName();
                result = senderParty;
                processTypePartyExtractor.getSenderParty();
                result = senderParty;
                process.getResponderParties();
                result = responder;
                responder.getName();
                result = receiverParty;
                processTypePartyExtractor.getReceiverParty();
                result = receiverParty;
            }
        };
        Assert.assertNull(cachingPModeProvider.findLegName("agreementName", "red_gw", "blue_gw", "service", "action"));
    }

    @Test
    public void testfindActionName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);

        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getActions();
            result = configuration.getBusinessProcesses().getActions();
        }};
        try {
            cachingPModeProvider.findActionName("action");
        } catch (EbMS3Exception ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.EbMS3ErrorCode.EBMS_0001);
        }
    }

    @Test
    public void testfindMpc() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};
        try {
            cachingPModeProvider.findMpc("no_mpc");
        } catch (EbMS3Exception ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.EbMS3ErrorCode.EBMS_0001);
        }
    }

    @Test
    public void testfindServiceName(@Mocked eu.domibus.ebms3.common.model.Service service) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, EbMS3Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getServices();
            result = configuration.getBusinessProcesses().getServices();
        }};
        try {
            cachingPModeProvider.findServiceName(service);
        } catch (EbMS3Exception ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.EbMS3ErrorCode.EBMS_0001);
        }
    }

    @Test
    public void testfindAgreement(@Injectable AgreementRef agreementRef) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, EbMS3Exception {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            agreementRef.getValue();
            result = "test";
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getAgreements();
            result = configuration.getBusinessProcesses().getAgreements();
        }};
        try {
            cachingPModeProvider.findAgreement(agreementRef);
        } catch (EbMS3Exception ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.EbMS3ErrorCode.EBMS_0001);
        }
    }

    @Test
    public void testGetPartyByIdentifier() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            result = configuration.getBusinessProcesses().getParties();
        }};
        Assert.assertNull(cachingPModeProvider.getPartyByIdentifier("test"));
    }

    @Test
    public void testGetSenderParty() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String partyKey = "red_gw";
        String pModeKey = "test";
        new Expectations(cachingPModeProvider) {{
            cachingPModeProvider.getSenderPartyNameFromPModeKey(pModeKey);
            result = partyKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            result = configuration.getBusinessProcesses().getParties();
        }};
        try {
            cachingPModeProvider.getSenderParty("test");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching sender party found with name:" + partyKey);
        }
    }

    @Test
    public void testGetReceiverParty(@Mocked PModeProvider pModeProvider, @Mocked MessageExchangeConfiguration messageExchangeConfiguration) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String partyKey = "red_gw";
        String pModeKey = "test";
        new Expectations() {{
            cachingPModeProvider.getReceiverPartyNameFromPModeKey(pModeKey);
            result = partyKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            result = configuration.getBusinessProcesses().getParties();
        }};
        try {
            cachingPModeProvider.getReceiverParty(pModeKey);
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching receiver party found with name" + partyKey);
        }
    }

    @Test
    public void testGetService(@Mocked PModeProvider pModeProvider, @Mocked MessageExchangeConfiguration messageExchangeConfiguration) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String serviceKey = "service";
        String pModeKey = "test";
        new Expectations() {{
            cachingPModeProvider.getServiceNameFromPModeKey(pModeKey);
            result = serviceKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getServices();
            result = configuration.getBusinessProcesses().getServices();
        }};
        try {
            cachingPModeProvider.getService(pModeKey);
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching service found with name: " + serviceKey);
        }
    }

    @Test
    public void testGetAction(@Mocked PModeProvider pModeProvider, @Mocked MessageExchangeConfiguration messageExchangeConfiguration) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String actionKey = "actionKey";
        String pModeKey = "test";
        new Expectations() {{
            cachingPModeProvider.getActionNameFromPModeKey(pModeKey);
            result = actionKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getActions();
            result = configuration.getBusinessProcesses().getActions();
        }};
        try {
            cachingPModeProvider.getAction(pModeKey);
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching action found with name: " + actionKey);
        }
    }

    @Test
    public void testGetAgreement(@Mocked PModeProvider pModeProvider, @Mocked MessageExchangeConfiguration messageExchangeConfiguration) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String agreementKey = "agreementKey";
        String pModeKey = "test";
        new Expectations() {{
            cachingPModeProvider.getAgreementRefNameFromPModeKey(pModeKey);
            result = agreementKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getAgreements();
            result = configuration.getBusinessProcesses().getAgreements();
        }};
        try {
            cachingPModeProvider.getAgreement(pModeKey);
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching agreement found with name: " + agreementKey);
        }
    }

    @Test
    public void testGetLegConfiguration(@Mocked PModeProvider pModeProvider, @Mocked MessageExchangeConfiguration messageExchangeConfiguration) throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        String legKey = "legKey";
        String pModeKey = "test";
        new Expectations() {{
            cachingPModeProvider.getLegConfigurationNameFromPModeKey(pModeKey);
            result = legKey;
            cachingPModeProvider.getConfiguration().getBusinessProcesses().getLegConfigurations();
            result = configuration.getBusinessProcesses().getLegConfigurations();
        }};
        try {
            cachingPModeProvider.getLegConfiguration(pModeKey);
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "no matching legConfiguration found with name: " + legKey);
        }
    }

    @Test
    public void testGetRetentionDownloadedByMpcURI() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(0, cachingPModeProvider.getRetentionDownloadedByMpcURI(ANOTHERMPC.toLowerCase()));
    }

    @Test
    public void testGetRetentionUndownloadedByMpcURI() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(-1, cachingPModeProvider.getRetentionUndownloadedByMpcURI(NONEXISTANTMPC));
    }

    @Test
    public void testFindUserMessageExchangeContextSenderNotFound(@Injectable UserMessage userMessage, @Injectable MSHRole mshRole, @Injectable PartyId partyId) throws EbMS3Exception {

        final Set<PartyId> fromPartyId = new HashSet<>();
        MSHRole mshRole1 = MSHRole.SENDING;
        new Expectations(cachingPModeProvider) {{
            userMessage.getPartyInfo().getFrom().getPartyId();
            result = fromPartyId;
            cachingPModeProvider.findPartyName(fromPartyId);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Sender party could not found for the value ", null, null);

        }};
        try {
            cachingPModeProvider.findUserMessageExchangeContext(userMessage, mshRole1, true);
            Assert.fail();
        } catch (EbMS3Exception ex) {
            assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, ex.getErrorCode());
            assertEquals(ex.getMshRole(), mshRole1);
        }
        new Verifications() {{
            userMessage.getPartyInfo().getFrom().getPartyId();
            times = 1;
            cachingPModeProvider.findPartyName(fromPartyId);
            times = 1;
        }};
    }

    @Test
    public void testFindUserMessageExchangeContextReceiverNotFound(@Injectable UserMessage userMessage, @Injectable MSHRole mshRole, @Injectable PartyId partyId) throws EbMS3Exception {

        final Set<PartyId> fromPartyId = new HashSet<>();
        PartyId partyId1 = new PartyId();
        partyId1.setValue("domibus-blue");
        fromPartyId.add(partyId1);
        final Set<PartyId> toPartyId = new HashSet<>();
        MSHRole mshRole1 = MSHRole.SENDING;

        new Expectations(cachingPModeProvider) {{
            userMessage.getPartyInfo().getFrom().getPartyId();
            result = fromPartyId;

            userMessage.getPartyInfo().getTo().getPartyId();
            result = toPartyId;

            cachingPModeProvider.findPartyName(fromPartyId);
            result = "domibus-blue";

            cachingPModeProvider.findPartyName(toPartyId);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Receiver party could not found for the value ", null, null);

        }};

        try {
            cachingPModeProvider.findUserMessageExchangeContext(userMessage, mshRole1, true);
            Assert.fail();
        } catch (EbMS3Exception ex) {
            assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, ex.getErrorCode());
            assertEquals(ex.getMshRole(), mshRole1);
        }

        new Verifications() {{
            userMessage.getPartyInfo().getTo().getPartyId();
            times = 1;
            userMessage.getPartyInfo().getFrom().getPartyId();
            times = 1;
            cachingPModeProvider.findPartyName(fromPartyId);
            times = 1;
            cachingPModeProvider.findPartyName(toPartyId);
            times = 1;
        }};
    }
}
