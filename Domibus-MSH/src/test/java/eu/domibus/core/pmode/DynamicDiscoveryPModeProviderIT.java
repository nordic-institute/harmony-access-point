package eu.domibus.core.pmode;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 4.1.4
 */
public class DynamicDiscoveryPModeProviderIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryPModeProviderIT.class);
    public static final String NAME = "Name";

    private DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider = new DynamicDiscoveryPModeProvider(null);

    private CachingPModeProvider cachingPModeProvider = new CachingPModeProvider(DomainService.DEFAULT_DOMAIN);

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(dynamicDiscoveryPModeProvider, "configuration", getConfiguration());
        ReflectionTestUtils.setField(cachingPModeProvider, "configuration", getConfiguration());
    }

    /**
     * Test case to show case the issue with the singleton {@link CachingPModeProvider#getConfiguration()}
     * <p>
     * getPartiesAndDoStuff Thread should start before modifyConfigSynchronized and finish after
     * <p>
     * To reproduce the {@link java.util.ConcurrentModificationException}, change the {@link BusinessProcesses#getParties()} to:
     * <p>
     * public List<Party> getParties() {
     * return this.parties;
     * }
     */
    @Test
    public void concurrentAccessReadWrite() throws ExecutionException, InterruptedException {
        Callable<List<Party>> getPartiesAndDoStuff = () -> {

            List<Party> parties;
            LOG.info("Start Thread getParties " + Thread.currentThread().getName());
            parties = dynamicDiscoveryPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            for (final Party party : parties) {
                sleepNicely(100);
            }
            LOG.info("End Thread getParties " + Thread.currentThread().getName());
            return parties;
        };

        Callable<Party> modifyConfigSynchronized = () -> {
            // Thread should start modifying the list after the getParties from the other thread
            sleepNicely(50);
            LOG.info("Start Thread updateConfigurationParty " + Thread.currentThread().getName());
            Party party = dynamicDiscoveryPModeProvider.updateConfigurationParty(NAME, "type", "enpoint");
            LOG.info("End Thread updateConfigurationParty " + Thread.currentThread().getName());
            return party;
        };

        ExecutorService ex = Executors.newFixedThreadPool(2);

        Future<List<Party>> get = ex.submit(getPartiesAndDoStuff);
        Future<Party> modify = ex.submit(modifyConfigSynchronized);

        try {
            List<Party> parties = get.get();
            Party party = modify.get();

            //Parties does not have the new party
            assertFalse(parties.stream().anyMatch(p -> NAME.equalsIgnoreCase(p.getName())));
            assertNotNull(party);
            assertEquals(NAME, party.getName());
        } catch (InterruptedException e) {
            LOG.info("InterruptedException during get");
        }

    }

    /**
     * This test is just an example to reproduce the issue described by EDELIVERY-6522 when there are concurrent access issues
     *
     * When fetched DDC metadata will be cached in concurrent safe way - this test must be also updated/removed
     *
     *
     * @throws Exception
     */
    @Test
    public void test_UpdateConfigurationParty_FindPartyName() throws Exception {

        final String partyName = "PIT000158";
        final String partyType = "urn:fdc:peppol.eu:2017:identifiers:ap";
        final String partyUrl = "https://notier.regione.emilia-romagna.it/oxalis/as4";
        PartyId partyId = new PartyId();
        partyId.setValue(partyName);
        partyId.setType(partyType);

        Callable<Party> updateConfigPartyTask = () -> {
            // update the configuration
            // but sleep a variable time before doing this
            int sleepTime = RandomUtils.nextInt(10, 51);
            LOG.info("Sleep first=[{}]", sleepTime);
            sleepNicely(sleepTime);
            LOG.info("Start Thread updateConfigurationParty " + Thread.currentThread().getName());
            Party party = dynamicDiscoveryPModeProvider.updateConfigurationParty(partyName, partyType, partyUrl);
            LOG.info("End Thread updateConfigurationParty " + Thread.currentThread().getName());
            return party;
        };

        int nbThreads = 50;
        ExecutorService executorUpdate = Executors.newFixedThreadPool(nbThreads);
        List<Callable<Party>> tasksList = new ArrayList<>();
        for (int i =0; i < nbThreads; i++) {
            tasksList.add(updateConfigPartyTask);
        }

        //update here
        try {
            List<Future<Party>> updateResult = executorUpdate.invokeAll(tasksList);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("InterruptedException during invokeAll");
        }

        //read here
        try {
            do {
                cachingPModeProvider.findPartyName(Collections.singletonList(partyId));
                Assert.fail("Exception expected");
            } while (executorUpdate.isTerminated());
        } catch (Exception e) {
            LOG.error("exception thrown", e);
            Assert.assertTrue(e instanceof EbMS3Exception);
            Assert.assertTrue(e.getMessage().contains("No matching party found"));
        }
    }


    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        BusinessProcesses businessProcesses = new BusinessProcesses();
        businessProcesses.setParties(new ArrayList<>(Arrays.asList(new Party(), new Party(), new Party())));
        configuration.setBusinessProcesses(businessProcesses);
        return configuration;
    }

    private void sleepNicely(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info(Thread.currentThread().getName() + " was interrupted during sleep");
        }
    }

}