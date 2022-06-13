package eu.domibus.core.message.pull;

import com.google.common.collect.Sets;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.core.message.pull.DomainPullFrequencyHelper.DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC_PREFIX;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DomainPullFrequencyHelperTest {

    @Tested
    private DomainPullFrequencyHelper domainPullFrequencyHelper = new DomainPullFrequencyHelper(new Domain("code", "name"));

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    private String defaultPullFrequency = "13";

    @Test
    public void setMpcNames_DefaultPullFrequency() {
        // GIVEN
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE); result = defaultPullFrequency;
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC_PREFIX + "defaultMPC"); result = defaultPullFrequency;
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME); result = "10";
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT); result = "10";
        }};

        // WHEN
        domainPullFrequencyHelper.setMpcNames(Sets.newHashSet("defaultMPC"));

        // THEN
        new Verifications() {{
            Map<String, MpcPullFrequency> mpcPullFrequencyMap = Deencapsulation.getField(domainPullFrequencyHelper, "mpcPullFrequencyMap");
            Assert.assertTrue("Should have populated the pull frequency map for the default MPC", mpcPullFrequencyMap.containsKey("defaultMPC"));

            Integer pullFrequency = Deencapsulation.getField(mpcPullFrequencyMap.get("defaultMPC"), "maxRequestsPerMpc");
            Assert.assertEquals("Should have used the correct custom frequency for the default MPC", Integer.valueOf(defaultPullFrequency), pullFrequency);
        }};
    }
    @Test
    public void setMpcNames_CustomPullFrequency() {
        // GIVEN
        final String customPullFrequency = "5";
        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE); result = defaultPullFrequency;
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC_PREFIX + "defaultMPC"); result = customPullFrequency;
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME); result = "10";
            domibusPropertyProvider.getProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT); result = "10";
        }};

        // WHEN
        domainPullFrequencyHelper.setMpcNames(Sets.newHashSet("defaultMPC"));

        // THEN
        new Verifications() {{
            Map<String, MpcPullFrequency> mpcPullFrequencyMap = Deencapsulation.getField(domainPullFrequencyHelper, "mpcPullFrequencyMap");
            Assert.assertTrue("Should have populated the pull frequency map for the default MPC", mpcPullFrequencyMap.containsKey("defaultMPC"));

            Integer pullFrequency = Deencapsulation.getField(mpcPullFrequencyMap.get("defaultMPC"), "maxRequestsPerMpc");
            Assert.assertEquals("Should have used the correct custom frequency for the default MPC", Integer.valueOf(customPullFrequency), pullFrequency);
        }};
    }
}
