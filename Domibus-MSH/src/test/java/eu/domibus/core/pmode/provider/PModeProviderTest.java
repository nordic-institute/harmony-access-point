package eu.domibus.core.pmode.provider;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.ConfigurationRawDAO;
import eu.domibus.core.pmode.validation.PModeValidationService;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PModeProviderTest {
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";

    @Tested
    private PModeProvider pModeProvider;

    @Injectable
    protected ConfigurationDAO configurationDAO;

    @Injectable
    protected ConfigurationRawDAO configurationRawDAO;

    @Injectable
    protected EntityManager entityManager;

    @Injectable
    protected SignalService signalService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    PModeValidationService pModeValidationService;

    @Injectable
    private JAXBContext jaxbContextConfig;

    @Injectable
    private MpcService mpcService;

    @Injectable
    private DomibusCacheService domibusCacheService;

    @Test
    public void test_checkSelfSending_DifferentAPs_False() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;

            domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_RECEIVER_SELF_SENDING_VALIDATION_ACTIVE);
            result = true;
        }};

        //tested method
        boolean selfSendingFlag = pModeProvider.checkSelfSending(pmodeKey);
        Assert.assertFalse("expected result should be false", selfSendingFlag);

        new FullVerifications() {
        };
    }

    @Test
    public void test_checkSelfSending_SameAPs_True() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, BLUE);

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;

            domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_RECEIVER_SELF_SENDING_VALIDATION_ACTIVE);
            result = true;
        }};

        //tested method
        boolean selfSendingFlag = pModeProvider.checkSelfSending(pmodeKey);
        Assert.assertTrue("expected result should be true", selfSendingFlag);

        new FullVerifications() {
        };
    }

    @Test
    public void test_checkSelfSending_DifferentAPsSameEndpoint_True() throws Exception {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final Party senderParty = getPartyFromConfiguration(configuration, BLUE);
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);
        receiverParty.setEndpoint(senderParty.getEndpoint().toLowerCase());

        new Expectations() {{
            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            pModeProvider.getSenderParty(pmodeKey);
            result = senderParty;

            domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_RECEIVER_SELF_SENDING_VALIDATION_ACTIVE);
            result = true;
        }};

        //tested method
        boolean selfSendingFlag = pModeProvider.checkSelfSending(pmodeKey);
        Assert.assertTrue("expected result should be true", selfSendingFlag);

        new FullVerifications() {
        };
    }


    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist");
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

    public LegConfiguration getLegFromConfiguration(Configuration configuration, String legName) {
        LegConfiguration result = null;
        for (LegConfiguration legConfiguration1 : configuration.getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legName, legConfiguration1.getName())) {
                result = legConfiguration1;
            }
        }
        return result;
    }


    public Party getPartyFromConfiguration(Configuration configuration, String partyName) {
        Party result = null;
        for (Party party : configuration.getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                result = party;
            }
        }
        return result;
    }

}