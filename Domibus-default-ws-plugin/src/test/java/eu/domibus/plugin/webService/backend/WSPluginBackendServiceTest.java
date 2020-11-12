package eu.domibus.plugin.webService.backend;

import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginBackendServiceTest {

    public static final String RECIPIENT = "recipient";
    public static final String MESSAGE_ID = "messageId";
    public static final String END_POINT = "endPoint";
    public static final int RETRY_MAX = 10;
    @Tested
    private WSPluginBackendService wsPluginBackendService;

    @Injectable
    private WSPluginMessageSender wsPluginMessageSender;

    @Injectable
    private WSPluginDispatchRulesService wsBackendRulesService;

    @Test
    public void sendSuccess(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            wsBackendRulesService.getRules(RECIPIENT);
            times = 1;
            result = Collections.singletonList(wsPluginDispatchRule);

            wsPluginDispatchRule.getTypes();
            result = Arrays.asList(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.MESSAGE_STATUS_CHANGE);
            wsPluginDispatchRule.getEndpoint();
            result = END_POINT;
            wsPluginDispatchRule.getRetryCount();
            result = RETRY_MAX;
        }};

        wsPluginBackendService.sendSuccess(MESSAGE_ID, RECIPIENT);

        new FullVerifications(){{
            WSBackendMessageLogEntity wsBackendMessageLogEntity;
            wsPluginMessageSender.sendMessageSuccess(wsBackendMessageLogEntity = withCapture());

            Assert.assertEquals(MESSAGE_ID, wsBackendMessageLogEntity.getMessageId());
            Assert.assertEquals(END_POINT, wsBackendMessageLogEntity.getEndpoint());
            Assert.assertEquals(RECIPIENT, wsBackendMessageLogEntity.getFinalRecipient());
            Assert.assertEquals(WSBackendMessageType.SEND_SUCCESS, wsBackendMessageLogEntity.getType());
            Assert.assertEquals(1, wsBackendMessageLogEntity.getSendAttempts());
            Assert.assertEquals(RETRY_MAX, wsBackendMessageLogEntity.getSendAttemptsMax());
        }};
    }

    @Test
    public void sendSuccess_noRecipient(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        wsPluginBackendService.sendSuccess(MESSAGE_ID, "");

        new FullVerifications(){};
    }

    @Test
    public void noRules(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations(){{
            wsBackendRulesService.getRules(RECIPIENT);
            times = 1;
            result = new ArrayList<>();
        }};

        wsPluginBackendService.sendSuccess("", RECIPIENT);

        new FullVerifications(){};
    }
}