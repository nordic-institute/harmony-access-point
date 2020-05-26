package eu.domibus.core.payload.persistence;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.PartInfo;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class PayloadPersistenceHelperTest {

    @Tested
    PayloadPersistenceHelper payloadPersistenceHelper;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Test
    public void testValidatePayloadSize_PayloadSizeGreater_ExpectedException(
            final @Mocked LegConfiguration legConfiguration,
            final @Mocked PartInfo partInfo) {
        final int partInfoLength = 100;
        final int payloadProfileMaxSize = 40;
        final String payloadProfileName = "testProfile";
        new Expectations() {{
            legConfiguration.getPayloadProfile().getName();
            result = payloadProfileName;

            legConfiguration.getPayloadProfile().getMaxSize();
            result = payloadProfileMaxSize;

            partInfo.getLength();
            result = partInfoLength;
        }};

        try {
            payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength());
            Assert.fail("exception expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof InvalidPayloadSizeException);
            Assert.assertEquals("[DOM_007]:Payload size [" + partInfoLength + "] is greater than the maximum value defined [" + payloadProfileMaxSize + "] for profile [" + payloadProfileName + "]",
                    e.getMessage());
        }
    }
}