package eu.domibus.core.pmode;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.junit.Assert.*;

public class DynamicDiscoveryPModeProviderStressIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryPModeProviderStressIT.class);
    public static final String NAME = "Name";
    private static DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider = new DynamicDiscoveryPModeProvider(null);
    private static boolean exceptionFound = false;
    private static int count = 0;

    @Rule
    public ConcurrentRule concurrently = new ConcurrentRule();
    @Rule
    public RepeatingRule rule = new RepeatingRule();

    @BeforeClass
    public static void setUp() {
        ReflectionTestUtils.setField(dynamicDiscoveryPModeProvider, "configuration", getConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        ReflectionTestUtils.setField(dynamicDiscoveryPModeProvider, "configuration", null);
    }

    @Test
    @Concurrent(count = 200)
    public void runsMultipleTimes2() throws InterruptedException {
        LOG.info("Start " + Thread.currentThread().getName());
        try {
            LOG.debug("Start Thread getParties " + Thread.currentThread().getName());
            List<Party> parties = dynamicDiscoveryPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            for (final Party party : parties) {
                // The thread should keep on reading the parties longer than the other thread is trying to modify the parties
                Thread.sleep(200);
            }
            LOG.debug("End Thread getParties " + Thread.currentThread().getName());


            // Thread should start modifying the list after the getParties from the other thread
            LOG.debug("Start Thread updateConfigurationParty " + Thread.currentThread().getName());
            Party party = dynamicDiscoveryPModeProvider.updateConfigurationParty(NAME, "type", "enpoint");
            LOG.debug("End Thread updateConfigurationParty " + Thread.currentThread().getName());

            //Parties does not have the new party
            assertFalse(parties.stream().anyMatch(p -> NAME.equalsIgnoreCase(p.getName())));
            assertNotNull(party);
            assertEquals(NAME, party.getName());
        } catch (ConcurrentModificationException e) {
            LOG.info("ConcurrentModificationException caught " + Thread.currentThread().getName());
            exceptionFound = true;
            count++;
        }
        LOG.info("End " + Thread.currentThread().getName());
    }

    @AfterClass
    public static void annotatedTestRunsMultipleTimes() {
        assertFalse(exceptionFound);
        LOG.info(count + " ConcurrentModificationException caught ");
    }

    private static Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        BusinessProcesses businessProcesses = new BusinessProcesses();
        businessProcesses.setParties(new ArrayList<>(Arrays.asList(new Party(), new Party(), new Party())));
        configuration.setBusinessProcesses(businessProcesses);
        return configuration;
    }
}