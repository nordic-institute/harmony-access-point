package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.common.MessageDeletedBatchEvent;
import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.MessageEvent;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.retry.WSPluginBackendRetryService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginBackendService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginBackendService.class);

    final WSPluginBackendRetryService retryService;
    final WSPluginDispatchRulesService wsBackendRulesService;

    final UserMessageExtService userMessageExtService;

    public WSPluginBackendService(WSPluginBackendRetryService retryService,
                                  WSPluginDispatchRulesService wsBackendRulesService,
                                  UserMessageExtService userMessageExtService) {
        this.retryService = retryService;
        this.wsBackendRulesService = wsBackendRulesService;
        this.userMessageExtService = userMessageExtService;
    }

    public void send(MessageEvent messageEvent, WSBackendMessageType messageType) {
        String messageId = messageEvent.getMessageId();
        String finalRecipient = messageEvent.getProps().get(MessageConstants.FINAL_RECIPIENT);
        String originalSender = messageEvent.getProps().get(MessageConstants.ORIGINAL_SENDER);
        if (StringUtils.isBlank(finalRecipient)) {
            LOG.warn("No recipient found for messageId: [{}]", messageEvent.getMessageId());
            return;
        }

        List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
        if (isEmpty(rules)) {
            LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            return;
        }

        for (WSPluginDispatchRule rule : rules) {
            if (rule.getTypes().contains(messageType)) {
                LOG.debug("Rule [{}] found for recipient [{}]", rule.getRuleName(), finalRecipient);
                retryService.send(messageId, finalRecipient, originalSender, rule, messageType);
            }
        }

    }

    public void send(MessageDeletedBatchEvent batchEvents, WSBackendMessageType messageType) {

        Map<String, List<String>> messageIdsPerRecipient = sortMessageIdsPerFinalRecipients(batchEvents);

        Set<Map.Entry<String, List<WSPluginDispatchRule>>> rulesForRecipients =
                getRulesForFinalRecipients(messageIdsPerRecipient).entrySet();
        for (Map.Entry<String, List<WSPluginDispatchRule>> rulesForOneRecipient : rulesForRecipients) {
            String finalRecipient = rulesForOneRecipient.getKey();
            List<String> messagesIdsForRecipient = messageIdsPerRecipient.get(rulesForOneRecipient.getKey());
            List<WSPluginDispatchRule> rulesForRecipient = rulesForOneRecipient.getValue();
            sendNotificationsForOneRecipient(finalRecipient, messagesIdsForRecipient, rulesForRecipient, messageType);
        }
    }

    protected Map<String, List<String>> sortMessageIdsPerFinalRecipients(MessageDeletedBatchEvent batchEvents) {
        Map<String, List<String>> messageIdsPerRecipient = new HashMap<>();
        for (MessageDeletedEvent deleteEvent : batchEvents.getMessageDeletedEvents()) {
            addMessageIdToMap(deleteEvent, messageIdsPerRecipient);
        }
        return messageIdsPerRecipient;
    }

    protected Map<String, List<WSPluginDispatchRule>> getRulesForFinalRecipients(Map<String, List<String>> messageIdsPerRecipient) {
        Map<String, List<WSPluginDispatchRule>> rulesPerRecipient = new HashMap<>();
        for (String finalRecipient : messageIdsPerRecipient.keySet()) {
            List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
            if (isEmpty(rules)) {
                LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            }
            rulesPerRecipient.put(finalRecipient, rules);
        }
        return rulesPerRecipient;
    }

    protected void sendNotificationsForOneRecipient(
            String finalRecipient,
            List<String> messageIds,
            List<WSPluginDispatchRule> value,
            WSBackendMessageType messageType) {
        for (WSPluginDispatchRule wsPluginDispatchRule : value) {
            sendNotificationsForOneRule(finalRecipient, messageIds, messageType, wsPluginDispatchRule);
        }
    }

    protected void sendNotificationsForOneRule(String finalRecipient, List<String> messageIds, WSBackendMessageType messageType, WSPluginDispatchRule wsPluginDispatchRule) {
        for (WSBackendMessageType type : wsPluginDispatchRule.getTypes()) {
            if (type == messageType) {
                retryService.send(messageIds, finalRecipient, wsPluginDispatchRule, messageType);
            }
        }
    }

    protected void addMessageIdToMap(MessageDeletedEvent batchEvent, Map<String, List<String>> messageIdGroupedByRecipient) {
        String finalRecipient = batchEvent.getProps().get(MessageConstants.FINAL_RECIPIENT);

        if (StringUtils.isBlank(finalRecipient)) {
            LOG.warn("No recipient found for batchEvent: [{}]", batchEvent);
            return;
        }
        List<String> messageIdsPerFinalRecipient = messageIdGroupedByRecipient.get(finalRecipient);
        if (isEmpty(messageIdsPerFinalRecipient)) {
            messageIdsPerFinalRecipient = new ArrayList<>();
        }
        messageIdsPerFinalRecipient.add(batchEvent.getMessageId());
        messageIdGroupedByRecipient.put(finalRecipient, messageIdsPerFinalRecipient);
    }
}
