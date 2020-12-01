package eu.domibus.plugin.webService.backend;

import eu.domibus.ext.services.UserMessageExtService;
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

    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String ORIGINAL_SENDER = "originalSender";
    public static final String MESSAGE_ID = "messageId";
    public static final String RULE_NAME = "ruleName";
    @Tested
    private WSPluginBackendService wsPluginBackendService;

    @Injectable
    private WSPluginBackendRetryService retryService;

    @Injectable
    private WSPluginDispatchRulesService wsBackendRulesService;

    @Injectable
    private UserMessageExtService userMessageExtService;

    @Test
    public void sendSuccess(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = FINAL_RECIPIENT;
            userMessageExtService.getOriginalSender(MESSAGE_ID);
            times = 1;
            result = ORIGINAL_SENDER;

            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT);
            times = 1;
            result = Collections.singletonList(wsPluginDispatchRule);


            wsPluginDispatchRule.getTypes();
            result = Arrays.asList(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.MESSAGE_STATUS_CHANGE);

            wsPluginDispatchRule.getRuleName();
            result = RULE_NAME;
        }};

        wsPluginBackendService.send(MESSAGE_ID, WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {{
            retryService.send(MESSAGE_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, wsPluginDispatchRule, WSBackendMessageType.SEND_SUCCESS);
            times = 1;
        }};
    }

    @Test
    public void sendSuccess_noRecipient(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = null;
        }};

        wsPluginBackendService.send(MESSAGE_ID, WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {
        };
    }

    @Test
    public void noRules(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations() {{

            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = FINAL_RECIPIENT;

            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT);
            times = 1;
            result = new ArrayList<>();
        }};

        wsPluginBackendService.send(MESSAGE_ID, WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {
        };
    }
}