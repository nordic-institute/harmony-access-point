package eu.domibus.plugin.webService.backend;

import eu.domibus.plugin.webService.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.webService.backend.reliability.retry.WSPluginBackendRetryService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
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
    private WSPluginBackendRetryService retryService;

    @Injectable
    private WSPluginDispatchRulesService wsBackendRulesService;

    @Test
    public void sendSuccess(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            wsBackendRulesService.getRulesByRecipient(RECIPIENT);
            times = 1;
            result = Collections.singletonList(wsPluginDispatchRule);

            wsPluginDispatchRule.getTypes();
            result = Arrays.asList(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.MESSAGE_STATUS_CHANGE);
        }};

        wsPluginBackendService.sendNotification(WSBackendMessageType.SEND_SUCCESS, MESSAGE_ID, RECIPIENT);

        new FullVerifications() {{
            retryService.sendNotification(MESSAGE_ID, RECIPIENT, wsPluginDispatchRule);
            times = 1;
        }};
    }

    @Test
    public void sendSuccess_noRecipient(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        wsPluginBackendService.sendNotification(WSBackendMessageType.SEND_SUCCESS, MESSAGE_ID, "");

        new FullVerifications() {
        };
    }

    @Test
    public void noRules(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            wsBackendRulesService.getRulesByRecipient(RECIPIENT);
            times = 1;
            result = new ArrayList<>();
        }};

        wsPluginBackendService.sendNotification(WSBackendMessageType.SEND_SUCCESS, "", RECIPIENT);

        new FullVerifications() {
        };
    }
}