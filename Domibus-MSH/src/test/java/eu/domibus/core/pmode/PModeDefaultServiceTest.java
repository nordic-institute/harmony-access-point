package eu.domibus.core.pmode;

import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.common.MSHRole;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PModeDefaultServiceTest {

    @Tested
    PModeDefaultService pModeDefaultService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private MessageExchangeService messageExchangeService;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Test
    public void testGetLegConfiguration(@Injectable final UserMessage userMessage,
                                        @Injectable final eu.domibus.common.model.configuration.LegConfiguration legConfigurationEntity) throws Exception {
        final String messageId = "1";
        final String pmodeKey = "1";
        final MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("1", ",", "", "", "", "");
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, anyBoolean);
            result = messageExchangeConfiguration;

            pModeProvider.getLegConfiguration(messageExchangeConfiguration.getPmodeKey());
            result = legConfigurationEntity;

        }};

        pModeDefaultService.getLegConfiguration(messageId);

        new Verifications() {{
            pModeDefaultService.convert(legConfigurationEntity);
        }};
    }

    @Test
    public void testUploadPModesXmlProcessingWithErrorException() throws XmlProcessingException {
        // Given
        byte[] file = new byte[]{1, 0, 1};
        XmlProcessingException xmlProcessingException = new XmlProcessingException("UnitTest1");
        xmlProcessingException.getErrors().add("error1");

        new Expectations() {{
            pModeProvider.updatePModes((byte[]) any, anyString);
            result = xmlProcessingException;

            pModeValidationHelper.getPModeValidationException(xmlProcessingException, "Failed to upload the PMode file due to: ");
            result = new PModeValidationException("Failed to upload the PMode file due to: ", null);
        }};

        // When
        try {
            pModeDefaultService.updatePModeFile(file, "description");
        } catch (PModeValidationException ex) {
            Assert.assertEquals("[DOM_003]:Failed to upload the PMode file due to: ", ex.getMessage());
//            Assert.assertEquals(1, ex.getIssues().size());
//            Assert.assertEquals("error1", ex.getIssues().get(0).getMessage());
        }

    }
}
