package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.common.MessageDeletedBatchEvent;
import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.MessageSendSuccessEvent;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.retry.WSPluginBackendRetryService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.plugin.webService.backend.WSBackendMessageType.DELETED_BATCH;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginBackendServiceTest {

    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String FINAL_RECIPIENT2 = "finalRecipient2";
    public static final String FINAL_RECIPIENT3 = "finalRecipient3";
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
        MessageSendSuccessEvent messageSendSuccessEvent = getMessageSendSuccessEvent(FINAL_RECIPIENT);
        new Expectations() {{

            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT);
            times = 1;
            result = Collections.singletonList(wsPluginDispatchRule);


            wsPluginDispatchRule.getTypes();
            result = Arrays.asList(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.MESSAGE_STATUS_CHANGE);

            wsPluginDispatchRule.getRuleName();
            result = RULE_NAME;
        }};

        wsPluginBackendService.send(messageSendSuccessEvent, WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {{
            retryService.send(MESSAGE_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, wsPluginDispatchRule, WSBackendMessageType.SEND_SUCCESS);
            times = 1;
        }};
    }

    private MessageSendSuccessEvent getMessageSendSuccessEvent(String finalRecipient) {
        HashMap<String, String> properties = new HashMap<>();
        if (StringUtils.isNotBlank(finalRecipient)) {
            properties.put(MessageConstants.FINAL_RECIPIENT, finalRecipient);
        }
        properties.put(MessageConstants.ORIGINAL_SENDER, ORIGINAL_SENDER);
        MessageSendSuccessEvent messageSendSuccessEvent = new MessageSendSuccessEvent(MESSAGE_ID, properties);
        return messageSendSuccessEvent;
    }

    @Test
    public void sendSuccess_noRecipient() {

        wsPluginBackendService.send(getMessageSendSuccessEvent(""), WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {
        };
    }

    @Test
    public void noRules() {
        new Expectations() {{
            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT);
            times = 1;
            result = new ArrayList<>();
        }};

        wsPluginBackendService.send(getMessageSendSuccessEvent(FINAL_RECIPIENT), WSBackendMessageType.SEND_SUCCESS);

        new FullVerifications() {
        };
    }

    @Test
    public void sendNotificationsForOneRule_empty(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        List<WSBackendMessageType> types = new ArrayList<>();
        new Expectations() {{
            wsPluginDispatchRule.getTypes();
            result = types;
        }};

        wsPluginBackendService.sendNotificationsForOneRule(
                FINAL_RECIPIENT,
                new ArrayList<>(),
                DELETED_BATCH,
                wsPluginDispatchRule);

        new FullVerifications() {
        };
    }

    @Test
    public void sendNotificationsForOneRule_1notification(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        List<WSBackendMessageType> types = Arrays.asList(WSBackendMessageType.DELETED, DELETED_BATCH);
        ArrayList<String> messageIds = new ArrayList<>();

        new Expectations() {{
            wsPluginDispatchRule.getTypes();
            result = types;
        }};

        wsPluginBackendService.sendNotificationsForOneRule(
                FINAL_RECIPIENT,
                messageIds,
                DELETED_BATCH,
                wsPluginDispatchRule);

        new FullVerifications() {{
            retryService.send(messageIds, FINAL_RECIPIENT, wsPluginDispatchRule, DELETED_BATCH);
            times = 1;
        }};
    }

    @Test
    public void sendNotificationsForOneRecipient(@Mocked WSPluginDispatchRule rule1,
                                                 @Mocked WSPluginDispatchRule rule2,
                                                 @Mocked WSBackendMessageType wsBackendMessageType) {
        List<String> messageIds = new ArrayList<>();
        new Expectations(wsPluginBackendService) {{
            wsPluginBackendService.sendNotificationsForOneRule(FINAL_RECIPIENT, messageIds, wsBackendMessageType, rule1);
            times = 1;

            wsPluginBackendService.sendNotificationsForOneRule(FINAL_RECIPIENT, messageIds, wsBackendMessageType, rule2);
            times = 1;
        }};
        wsPluginBackendService.sendNotificationsForOneRecipient(FINAL_RECIPIENT, messageIds, Arrays.asList(rule1, rule2), wsBackendMessageType);
        new FullVerifications() {
        };
    }

    @Test
    public void getRulesForFinalRecipients(@Mocked WSPluginDispatchRule rule1,
                                           @Mocked WSPluginDispatchRule rule2,
                                           @Mocked WSPluginDispatchRule rule3) {
        Map<String, List<String>> messageIdsPerRecipient = new HashMap<>();
        messageIdsPerRecipient.put(FINAL_RECIPIENT, Arrays.asList("1", "2"));
        messageIdsPerRecipient.put(FINAL_RECIPIENT2, Arrays.asList("1", "2"));
        messageIdsPerRecipient.put(FINAL_RECIPIENT3, Arrays.asList("1", "2"));
        List<WSPluginDispatchRule> rulesRecipient = Arrays.asList(rule1, rule2);
        List<WSPluginDispatchRule> rulesRecipient2 = Arrays.asList(rule3, rule2);

        new Expectations() {{
            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT);
            this.result = rulesRecipient;

            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT2);
            this.result = rulesRecipient2;

            wsBackendRulesService.getRulesByRecipient(FINAL_RECIPIENT3);
            this.result = new ArrayList<>();
        }};

        Map<String, List<WSPluginDispatchRule>> rulesForFinalRecipients =
                wsPluginBackendService.getRulesForFinalRecipients(messageIdsPerRecipient);

        new FullVerifications() {
        };

        Assert.assertEquals(3, rulesForFinalRecipients.size());
        Assert.assertEquals(rulesRecipient, rulesForFinalRecipients.get(FINAL_RECIPIENT));
        Assert.assertEquals(rulesRecipient2, rulesForFinalRecipients.get(FINAL_RECIPIENT2));
        Assert.assertEquals(0, rulesForFinalRecipients.get(FINAL_RECIPIENT3).size());
    }

    @Test
    public void sortMessageIdsPerFinalRecipients() {
        MessageDeletedBatchEvent messageDeletedBatchEvent = new MessageDeletedBatchEvent();
        List<MessageDeletedEvent> messageIdsPerRecipient = Stream
                .of("1", "2")
                .map(s -> getMessageDeletedEvent(s, FINAL_RECIPIENT))
                .collect(Collectors.toList());
        messageIdsPerRecipient.add(getMessageDeletedEvent("3", FINAL_RECIPIENT2));
        messageDeletedBatchEvent.setMessageDeletedEvents(messageIdsPerRecipient);

        Map<String, List<String>> stringListMap = wsPluginBackendService.sortMessageIdsPerFinalRecipients(messageDeletedBatchEvent);

        Assert.assertThat(stringListMap.get(FINAL_RECIPIENT), CoreMatchers.hasItems("1", "2"));
        Assert.assertEquals(2, stringListMap.get(FINAL_RECIPIENT).size());
        Assert.assertThat(stringListMap.get(FINAL_RECIPIENT2), CoreMatchers.hasItems("3"));
        Assert.assertEquals(1, stringListMap.get(FINAL_RECIPIENT2).size());
        new FullVerifications() {
        };
    }

    private MessageDeletedEvent getMessageDeletedEvent(String s, String finalRecipient) {
        MessageDeletedEvent messageDeletedEvent = new MessageDeletedEvent();
        messageDeletedEvent.setMessageId(s);
        messageDeletedEvent.addProperty(MessageConstants.FINAL_RECIPIENT, finalRecipient);
        return messageDeletedEvent;
    }

    @Test
    public void addMessageIdToMap() {
        HashMap<String, List<String>> messageIdGroupedByRecipient = new HashMap<>();

        wsPluginBackendService.addMessageIdToMap(getMessageDeletedEvent(MESSAGE_ID, FINAL_RECIPIENT), messageIdGroupedByRecipient);

        Assert.assertEquals(1, messageIdGroupedByRecipient.size());
        Assert.assertEquals(1, messageIdGroupedByRecipient.get(FINAL_RECIPIENT).size());
        Assert.assertEquals(MESSAGE_ID, messageIdGroupedByRecipient.get(FINAL_RECIPIENT).get(0));

    }

    @Test
    public void addMessageIdToMap_emptyRecipient() {
        HashMap<String, List<String>> messageIdGroupedByRecipient = new HashMap<>();

        wsPluginBackendService.addMessageIdToMap(new MessageDeletedEvent(), messageIdGroupedByRecipient);

        Assert.assertEquals(0, messageIdGroupedByRecipient.size());
    }

    @Test
    public void send(@Mocked WSPluginDispatchRule rule1,
                     @Mocked WSPluginDispatchRule rule2,
                     @Mocked WSPluginDispatchRule rule3) {
        MessageDeletedBatchEvent messageDeletedBatchEvent = new MessageDeletedBatchEvent();
        Stream<String> stringStream = Stream
                .of("1", "2", "3");
        List<MessageDeletedEvent> messageIdsPerRecipient = stringStream
                .map(s -> getMessageDeletedEvent(s, FINAL_RECIPIENT))
                .collect(Collectors.toList());
        messageDeletedBatchEvent.setMessageDeletedEvents(messageIdsPerRecipient);

        List<String> messageIds = new ArrayList<>();
        Map<String, List<String>> sorted = new HashMap<>();
        List<String> msgRecipient1 = Arrays.asList("1", "2");
        List<String> msgRecipient2 = Arrays.asList("1", "2");
        sorted.put(FINAL_RECIPIENT, msgRecipient1);
        sorted.put(FINAL_RECIPIENT2, msgRecipient2);
        Map<String, List<WSPluginDispatchRule>> rules = new HashMap<>();
        List<WSPluginDispatchRule> rulesRecipient = Arrays.asList(rule1, rule2);
        List<WSPluginDispatchRule> rulesRecipient2 = Arrays.asList(rule3, rule2);
        rules.put(FINAL_RECIPIENT, rulesRecipient);
        rules.put(FINAL_RECIPIENT2, rulesRecipient2);

        new Expectations(wsPluginBackendService) {{
            wsPluginBackendService.sortMessageIdsPerFinalRecipients(messageDeletedBatchEvent);
            result = sorted;

            wsPluginBackendService.getRulesForFinalRecipients(sorted);
            result = rules;

            wsPluginBackendService.sendNotificationsForOneRecipient(FINAL_RECIPIENT, msgRecipient1, rulesRecipient, DELETED_BATCH);
            times = 1;
            wsPluginBackendService.sendNotificationsForOneRecipient(FINAL_RECIPIENT2, msgRecipient2, rulesRecipient2, DELETED_BATCH);
            times = 1;
        }};
        wsPluginBackendService.send(messageDeletedBatchEvent, DELETED_BATCH);

        new FullVerifications() {
        };
    }
}