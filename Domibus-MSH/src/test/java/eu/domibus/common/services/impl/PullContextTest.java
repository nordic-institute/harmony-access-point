package eu.domibus.common.services.impl;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.util.PojoInstaciatorUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static eu.domibus.common.services.impl.PullRequestStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class PullContextTest {

    @Test
    public void checkProcessValidityWithMoreThanOneLegAndDifferentReponder() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}"));
        assertEquals(false, pullContext.isValid());
        assertEquals(2, pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullContext.getPullRequestStatuses().contains(NO_RESPONDER));
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}", "responderParties{[name:resp1];[name:resp2]}"));
        assertEquals(false, pullContext.isValid());
        assertEquals(2, pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
        assertTrue(pullContext.getPullRequestStatuses().contains(TOO_MANY_RESPONDER));
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1];[name:leg2]}", "responderParties{[name:resp1]}"));
        assertEquals(false, pullContext.isValid());
        assertEquals(1, pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(MORE_THAN_ONE_LEG_FOR_THE_SAME_MPC));
    }

    @Test
    public void checkProcessValidityWithZeroLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class));
        assertEquals(false, pullContext.isValid());
    }

    @Test
    public void checkProcessWithNoLegs() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "responderParties{[name:resp1]}"));
        assertEquals(false, pullContext.isValid());
        assertEquals(1, pullContext.getPullRequestStatuses().size());
        assertTrue(pullContext.getPullRequestStatuses().contains(NO_PROCESS_LEG));
    }

    @Test
    public void checkProcessValidityWithOneLeg() throws Exception {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1]}", "responderParties{[name:resp1]}"));
        assertEquals(true, pullContext.isValid());
        assertTrue(pullContext.getPullRequestStatuses().contains(ONE_MATCHING_PROCESS));
    }

    @Test
    public void filterLegOnMpc() {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}"));
        pullContext.setMpcQualifiedName("qn1");
        LegConfiguration legConfiguration = pullContext.filterLegOnMpc();
        assertEquals("qn1", legConfiguration.getDefaultMpc().getQualifiedName());
    }

    @Test
    public void createProcessWarningMessage() {
        PullContext pullContext = new PullContext();
        assertEquals(false, pullContext.isValid());
        assertTrue(pullContext.getPullRequestStatuses().contains(NO_PROCESSES));
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class));
        assertEquals(false, pullContext.isValid());
        assertTrue(pullContext.createProcessWarningMessage().contains("No leg configuration found"));
        assertTrue(pullContext.createProcessWarningMessage().contains("No responder configured"));
    }

    @Test
    public void send() {
        PullContext pullContext = new PullContext();
        pullContext.setProcess(PojoInstaciatorUtil.instanciate(Process.class, "agreement[name:agr1]", "legs{[name:leg1,defaultMpc[name:test1,qualifiedName:qn1]];[name:leg2,defaultMpc[name:test2,qualifiedName:qn2]]}", "responderParties{[name:resp1]}", "initiatorParties{[name:init1];[name:init2]}"));
        pullContext.setResponder(PojoInstaciatorUtil.instanciate(Party.class, " [name:resp1]"));
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        PullContextCommand mock = Mockito.mock(PullContextCommand.class);
        pullContext.send(mock);
        Mockito.verify(mock, Mockito.times(4)).execute(argument.capture());
        List<Map> allValues = argument.getAllValues();

        TestResult testResult = new TestResult("qn1", "resp1:init1:Mock:Mock:agr1:leg1", "false");
        testResult.
                chain(new TestResult("qn1", "resp1:init2:Mock:Mock:agr1:leg1", "false")).
                chain(new TestResult("qn2", "resp1:init1:Mock:Mock:agr1:leg2", "false")).
                chain(new TestResult("qn2", "resp1:init2:Mock:Mock:agr1:leg2", "false"));
        for (Map allValue : allValues) {
            assertTrue(testResult.testSucced(allValue));
        }
    }


}