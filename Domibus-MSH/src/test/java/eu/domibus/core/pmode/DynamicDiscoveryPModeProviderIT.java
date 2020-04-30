package eu.domibus.core.pmode;

import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class DynamicDiscoveryPModeProviderIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryPModeProviderIT.class);
    public static final String NAME = "Name";

    private DynamicDiscoveryPModeProvider dynamicDiscoveryPModeProvider = new DynamicDiscoveryPModeProvider(null);

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(dynamicDiscoveryPModeProvider, "configuration", getConfiguration());
    }

    /**
     * Test case to show case the issue with the singleton {@link CachingPModeProvider#getConfiguration()}
     *
     * getPartiesAndDoStuff Thread should start before modifyConfigSynchronized and finish after
     *
     * To reproduce the {@link java.util.ConcurrentModificationException}, change the {@link BusinessProcesses#getParties()} to:
     *
     *    public List<Party> getParties() {
     *         return this.parties;
     *     }
     *
     *    java.util.concurrent.ExecutionException: java.util.ConcurrentModificationException
     *               at java.util.concurrent.FutureTask.report(FutureTask.java:122)
     *               at java.util.concurrent.FutureTask.get(FutureTask.java:192)
     *               at eu.domibus.core.pmode.DynamicDiscoveryPModeProviderConfigurationTest.concurrentAccessReadWrite(DynamicDiscoveryPModeProviderConfigurationTest.java:56)
     *               at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     *               at java.lang.reflect.Method.invoke(Method.java:498)
     *               at org.mockito.internal.runners.JUnit45AndHigherRunnerImpl.run(JUnit45AndHigherRunnerImpl.java:37)
     *               at org.mockito.runners.MockitoJUnitRunner.run(MockitoJUnitRunner.java:62)
     *               at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
     *               at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:33)
     *               at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:230)
     *               at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:58)
     *    Caused by: java.util.ConcurrentModificationException
     *               at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:901)
     *               at java.util.ArrayList$Itr.next(ArrayList.java:851)
     *               at eu.domibus.core.pmode.DynamicDiscoveryPModeProviderConfigurationTest.lambda$concurrentAccessReadWrite$1(DynamicDiscoveryPModeProviderConfigurationTest.java:44)
     *               at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     *               at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     *               at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     *               at java.lang.Thread.run(Thread.java:745)
     */
    @Test
    public void concurrentAccessReadWrite() throws ExecutionException, TimeoutException, InterruptedException {
        Callable<List<Party>> getPartiesAndDoStuff = () -> {

            LOG.info("Start Thread getParties " + Thread.currentThread().getName());
            List<Party> parties = dynamicDiscoveryPModeProvider.getConfiguration().getBusinessProcesses().getParties();
            for (final Party party : parties) {
                // The thread should keep on reading the parties longer than the other thread is trying to modify the parties
                Thread.sleep(200);
            }
            LOG.info("End Thread getParties " + Thread.currentThread().getName());
            return parties;
        };

        Callable<Party> modifyConfigSynchronized = () -> {
            // Thread should start modifying the list after the getParties from the other thread
            Thread.sleep(50);
            LOG.info("Start Thread updateConfigurationParty " + Thread.currentThread().getName());
            Party party = dynamicDiscoveryPModeProvider.updateConfigurationParty(NAME, "type", "enpoint");
            LOG.info("End Thread updateConfigurationParty " + Thread.currentThread().getName());
            return party;
        };

        ExecutorService ex = Executors.newFixedThreadPool(2);

        Future<List<Party>> get = ex.submit(getPartiesAndDoStuff);
        Future<Party> modify = ex.submit(modifyConfigSynchronized);

        List<Party> parties = get.get();
        Party party = modify.get();

        //Parties does not have the new party
        assertFalse(parties.stream().anyMatch(p -> NAME.equalsIgnoreCase(p.getName())));
        assertNotNull(party);
        assertEquals(NAME, party.getName());
    }

    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        BusinessProcesses businessProcesses = new BusinessProcesses();
        businessProcesses.setParties(new ArrayList<>(Arrays.asList(new Party(), new Party(), new Party())));
        configuration.setBusinessProcesses(businessProcesses);
        return configuration;
    }
}