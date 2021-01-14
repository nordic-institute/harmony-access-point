package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.common.MessageDeletedBatchEvent;
import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.MessageEvent;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.retry.WSPluginBackendScheduleRetryService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.PUSH_ENABLED;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginBackendService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginBackendService.class);

    final WSPluginBackendScheduleRetryService scheduleService;
    final WSPluginDispatchRulesService wsBackendRulesService;

    final UserMessageExtService userMessageExtService;
    final WSPluginPropertyManager wsPluginPropertyManager;

    public WSPluginBackendService(WSPluginBackendScheduleRetryService scheduleService,
                                  WSPluginDispatchRulesService wsBackendRulesService,
                                  WSPluginPropertyManager wsPluginPropertyManager,
                                  UserMessageExtService userMessageExtService) {
        this.scheduleService = scheduleService;
        this.wsBackendRulesService = wsBackendRulesService;
        this.userMessageExtService = userMessageExtService;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
    }

    public boolean send(MessageEvent messageEvent, WSBackendMessageType messageType) {
        String pushEnabled = wsPluginPropertyManager.getKnownPropertyValue(PUSH_ENABLED);
        LOG.debug("Push to backend is: [{}]", pushEnabled);
        if (!toBoolean(pushEnabled)) {
            return false;
        }
        String messageId = messageEvent.getMessageId();
        String finalRecipient = messageEvent.getProps().get(MessageConstants.FINAL_RECIPIENT);
        String originalSender = messageEvent.getProps().get(MessageConstants.ORIGINAL_SENDER);
        if (StringUtils.isBlank(finalRecipient)) {
            LOG.warn("No recipient found for messageId: [{}]", messageEvent.getMessageId());
            return false;
        }

        List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
        if (isEmpty(rules)) {
            LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            return false;
        }

        for (WSPluginDispatchRule rule : rules) {
            if (rule.getTypes().contains(messageType)) {
                LOG.debug("Rule [{}] found for recipient [{}] and messageType [{}]", rule.getRuleName(), finalRecipient, messageType);
                scheduleService.schedule(messageId, finalRecipient, originalSender, rule, messageType);
                return true;
            }
        }

        return false;
    }

    public void send(MessageDeletedBatchEvent batchEvents, WSBackendMessageType messageType) {

        Map<String, List<String>> messageIdsPerRecipient = sortMessageIdsPerFinalRecipients(batchEvents);

        List<RulesPerRecipient> rulesForRecipients =
                getRulesForFinalRecipients(messageIdsPerRecipient);
        for (RulesPerRecipient rulesForOneRecipient : rulesForRecipients) {
            String finalRecipient = rulesForOneRecipient.getFinalRecipient();
            List<String> messagesIdsForRecipient = messageIdsPerRecipient.get(finalRecipient);
            List<WSPluginDispatchRule> rulesForRecipient = rulesForOneRecipient.getRules();
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

    protected List<RulesPerRecipient> getRulesForFinalRecipients(Map<String, List<String>> messageIdsPerRecipient) {
        List<RulesPerRecipient> rulesPerRecipient = new ArrayList<>();
        for (String finalRecipient : messageIdsPerRecipient.keySet()) {
            List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
            if (isEmpty(rules)) {
                LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            }
            rulesPerRecipient.add(new RulesPerRecipient(finalRecipient, rules));
        }
        return rulesPerRecipient;
    }

    protected void sendNotificationsForOneRecipient(
            String finalRecipient,
            List<String> messageIds,
            List<WSPluginDispatchRule> rules,
            WSBackendMessageType messageType) {
        for (WSPluginDispatchRule wsPluginDispatchRule : rules) {
            sendNotificationsForOneRule(finalRecipient, messageIds, messageType, wsPluginDispatchRule);
        }
    }

    protected void sendNotificationsForOneRule(String finalRecipient, List<String> messageIds, WSBackendMessageType messageType, WSPluginDispatchRule wsPluginDispatchRule) {
        for (WSBackendMessageType type : wsPluginDispatchRule.getTypes()) {
            if (type == messageType) {
                scheduleService.schedule(messageIds, finalRecipient, wsPluginDispatchRule, messageType);
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
