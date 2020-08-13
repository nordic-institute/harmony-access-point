package eu.domibus.core.metrics;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MetricsHelperTest {

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private AuthUtils authUtils;

    @Tested
    MetricsHelper metricsHelper;

    @Test
    public void test_showJMSCounts_ST() {
        new Expectations() {{
            domibusConfigurationService.isSingleTenant();
            result = true;
        }};

        Assert.assertTrue(metricsHelper.showJMSCounts());
        new FullVerifications() {{

        }};
    }

    @Test
    public void test_showJMSCounts_MT() {
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            result = true;

            authUtils.isSuperAdmin();
            result = true;
            result = false;
        }};

        Assert.assertTrue(metricsHelper.showJMSCounts());
        Assert.assertFalse(metricsHelper.showJMSCounts());
        new FullVerifications() {{
            domibusConfigurationService.isSingleTenant();
        }};
    }
}