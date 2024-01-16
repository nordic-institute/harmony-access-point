package eu.domibus.core.pmode.provider;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.ebms3.MessageExchangePattern;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.property.DomibusPropertyResourceHelperImpl;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;
import static org.junit.Assert.*;


/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
@Transactional
public class CachingPmodeProviderTestIT extends AbstractIT {

    @Autowired
    protected PModeProviderFactoryImpl pModeProviderFactory;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    DomibusPropertyResourceHelperImpl configurationPropertyResourceHelper;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Before
    public void setUp() throws Exception {
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "true");
    }

    @After
    public void clean() {
        domibusPropertyProvider.setProperty(domainContextProvider.getCurrentDomain(), DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "false");
    }

    @Test
    public void testX() throws XmlProcessingException, IOException {
        uploadPmode();
        pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        List<String> list = pModeProvider.findPartiesByInitiatorServiceAndAction("domibus-blue", Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, getPushMeps());
        assertTrue(list.size() == 1);
        assertTrue(list.contains("domibus-red"));

        List<String> list2 = pModeProvider.findPartiesByResponderServiceAndAction("domibus-red", Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION, getPushMeps());
        assertTrue(list2.size() == 1);
        assertTrue(list2.contains("domibus-blue"));
    }

    private List<MessageExchangePattern> getPushMeps() {
        List<MessageExchangePattern> meps = new ArrayList<>();
        meps.add(MessageExchangePattern.ONE_WAY_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PUSH);
        meps.add(MessageExchangePattern.TWO_WAY_PUSH_PULL);
        meps.add(MessageExchangePattern.TWO_WAY_PULL_PUSH);
        return meps;
    }

    @Test
    public void checkMpcMismatch() {
        LegConfiguration legConfiguration = new LegConfiguration();
        final Mpc mpc = new Mpc();
        mpc.setQualifiedName("defaultMpc1");
        legConfiguration.setDefaultMpc(mpc);
        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, null, null, null, "defaultMpc");
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        Set<String> mismatchedMPcs = new HashSet<>();
        String DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED = "domibus.pmode.legconfiguration.mpc.validation.enabled";
        DomibusProperty initialValue = configurationPropertyResourceHelper.getProperty(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED);
        configurationPropertyResourceHelper.setPropertyValue(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED, true, "false");

        boolean matchedMpc = pmodeProvider.checkMpcMismatch(legConfiguration, legFilterCriteria, mismatchedMPcs);

        assertEquals(mismatchedMPcs.size(), 1);
        assertFalse(matchedMpc);
        configurationPropertyResourceHelper.setPropertyValue(DOMIBUS_PMODE_LEGCONFIGURATION_MPC_VALIDATION_ENABLED, true, initialValue.getValue());
    }

    @Test
    public void filterMatchingLegConfigurations() {
        LegConfiguration legConfiguration1 = new LegConfiguration();
        LegConfiguration legConfiguration2 = new LegConfiguration();

        List<Process> matchingProcessesList = new ArrayList<>();
        Process process = new Process();
        process.setName("tc1Process");
        LinkedHashSet<LegConfiguration> candidateLegs = new LinkedHashSet<>();

        legConfiguration1.setName("leg0");
        final Mpc mpc1 = new Mpc();
        mpc1.setQualifiedName("defaultMpc");
        legConfiguration1.setDefaultMpc(mpc1);
        final Service service1 = new Service();
        service1.setName("testService0");
        service1.setValue("bdx:noprocess");
        service1.setServiceType("tc0");
        legConfiguration1.setService(service1);

        Action action1 = new Action();
        action1.setName("tc0Action");
        legConfiguration1.setAction(action1);

        legConfiguration2.setName("leg1");
        final Mpc mpc2 = new Mpc();
        mpc2.setQualifiedName("defaultMpc");
        legConfiguration2.setDefaultMpc(mpc2);
        final Service service2 = new Service();
        service2.setName("testService");
        service2.setValue("bdx:noprocess");
        service2.setServiceType("tc1");
        legConfiguration2.setService(service2);
        Action action2 = new Action();
        action2.setName("tc1Action");
        legConfiguration2.setAction(action2);

        candidateLegs.add(legConfiguration1);
        candidateLegs.add(legConfiguration2);
        process.setLegs(candidateLegs);
        matchingProcessesList.add(process);

        LegFilterCriteria legFilterCriteria = new LegFilterCriteria(null, null, null, null, null, "testService", "tc1Action", null, "defaultMpc");
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        Set<LegConfiguration> legConfigurationList = pmodeProvider.filterMatchingLegConfigurations(matchingProcessesList, legFilterCriteria);

        assertEquals(1, legConfigurationList.size());
        assertEquals("tc1Action", legConfigurationList.iterator().next().getAction().getName());
    }

    @Test
    public void getMaxRetryTimeout_defaultRetryAwareness() throws Exception {
        // GIVEN
        uploadPmode();
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        // WHEN
        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout();

        // THEN
        Assert.assertEquals("Should have returned the default maximum retry timeout in minutes", 12, maxRetryTimeout);
    }

    @Test
    public void getMaxRetryTimeout_customRetryAwareness() throws Exception {
        // GIVEN
        Map<String, String> replacements = new HashMap<>();
        replacements.put("retry=\".*\"", "retry=\"2;4;CONSTANT\"");
        uploadPmode(null, replacements);
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        // WHEN
        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout();

        // THEN
        Assert.assertEquals("Should have returned the correct maximum retry timeout in minutes when custom retry awareness set up", 2, maxRetryTimeout);
    }

    @Test
    public void getMaxRetryTimeout_noRetryAwareness() throws Exception {
        // GIVEN
        Map<String, String> replacements = new HashMap<>();
        replacements.put("retry=\".*\"", "");
        uploadPmode(null, replacements);
        final CachingPModeProvider pmodeProvider = (CachingPModeProvider) pModeProviderFactory.createDomainPModeProvider(domainContextProvider.getCurrentDomain());

        // WHEN
        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout();

        // THEN
        Assert.assertEquals("Should have returned the default maximum retry timeout in minutes when no custom retry awareness set up", 0, maxRetryTimeout);
    }
}
