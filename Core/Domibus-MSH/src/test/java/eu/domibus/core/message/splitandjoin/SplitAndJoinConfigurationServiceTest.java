package eu.domibus.core.message.splitandjoin;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)

public class SplitAndJoinConfigurationServiceTest {

    @Tested
    SplitAndJoinConfigurationService splitAndJoinConfigurationService;

    @Test
    public void mayUseSplitAndJoin(@Injectable LegConfiguration legConfiguration, @Injectable Splitting splitting) {
        new Expectations() {{
            legConfiguration.getSplitting();
            result = splitting;
        }};

        Assert.assertTrue(splitAndJoinConfigurationService.mayUseSplitAndJoin(legConfiguration));
    }

}
