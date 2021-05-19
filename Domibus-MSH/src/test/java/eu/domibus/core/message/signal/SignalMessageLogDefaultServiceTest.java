package eu.domibus.core.message.signal;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.SignalMessageLog;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.MshRoleDao;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class SignalMessageLogDefaultServiceTest {

    @Tested
    SignalMessageLogDefaultService signalMessageLogDefaultService;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageStatusDao messageStatusDao;

    @Injectable
    MshRoleDao mshRoleDao;

    @Parameterized.Parameter(0)
    public String userMessageService;

    @Parameterized.Parameter(1)
    public String userMessageAction;

    @Parameterized.Parameters(name = "{index}: usermessageService=\"{0}\" usermessageAction=\"{1}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {"service", "action"},
                {Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION}
        });
    }

    @Test
    public void testSave() {
        final String messageId = "1";
        final String signalMessageId = "signal-" + messageId;
        SignalMessage signalMessage = new SignalMessage();
        signalMessage.setSignalMessageId(signalMessageId);
        signalMessage.setRefToMessageId(messageId);
        signalMessageLogDefaultService.save(signalMessage, userMessageService, userMessageAction);

        new Verifications() {{
            SignalMessageLog signalMessageLog;
            signalMessageLogDao.create(signalMessageLog = withCapture());

            Assert.assertEquals(signalMessageId, signalMessageLog.getSignalMessage().getSignalMessageId());
            Assert.assertEquals(messageId, signalMessageLog.getSignalMessage().getRefToMessageId());
        }};
    }
}
