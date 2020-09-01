package eu.domibus.core.status;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.AssertionBuilderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class DomibusStatusServiceTest {

    @Injectable
    private Bus busCore;

    @Tested
    private DomibusStatusService domibusStatusService;

    @Test
    public void testReady(@Mocked final org.apache.neethi.PolicyBuilder policyBuilder,
                          @Mocked final AssertionBuilderFactory assertionBuilderFactory) {
        new Expectations() {{
            busCore.getExtension(PolicyBuilder.class);
            this.result = policyBuilder;
            policyBuilder.getAssertionBuilderFactory();
            result = assertionBuilderFactory;
        }};
        assertFalse(domibusStatusService.isNotReady());
    }

    @Test
    public void testNotReady(@Mocked final org.apache.neethi.PolicyBuilder policyBuilder) {
        new Expectations() {{
            busCore.getExtension(PolicyBuilder.class);
            this.result = policyBuilder;
            policyBuilder.getAssertionBuilderFactory();
            result = null;
        }};
        assertTrue(domibusStatusService.isNotReady());
    }

}