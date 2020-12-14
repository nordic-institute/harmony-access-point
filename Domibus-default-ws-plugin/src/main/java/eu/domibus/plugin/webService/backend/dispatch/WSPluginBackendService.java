package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    public void send(String messageId, WSBackendMessageType... messageTypes) {
        String finalRecipient = userMessageExtService.getFinalRecipient(messageId);
        if (StringUtils.isBlank(finalRecipient)) {
            LOG.warn("No recipient found for messageId: [{}]", messageId);
            return;
        }

        List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
        if (isEmpty(rules)) {
            LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            return;
        }

        String originalSender = userMessageExtService.getOriginalSender(messageId);

        for (WSBackendMessageType messageType : messageTypes) {
            for (WSPluginDispatchRule rule : rules) {
                if (rule.getTypes().contains(messageType)) {
                    LOG.debug("Rule [{}] found for recipient [{}]", rule.getRuleName(), finalRecipient);
                    retryService.send(messageId, finalRecipient, originalSender, rule, messageType);
                }
            }
        }

    }

    public void send(List<String> messageIds, WSBackendMessageType... messageTypes) {
        Map<String, List<String>> messageIdsPerRecipient = new HashMap<>();
        for (String messageId : messageIds) {
            addMessageIdToMap(messageId, messageIdsPerRecipient);
        }

        Map<String, List<WSPluginDispatchRule>> rulesPerRecipient = new HashMap<>();
        for (String finalRecipient : messageIdsPerRecipient.keySet()) {
            List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(finalRecipient);
            if (isEmpty(rules)) {
                LOG.warn("No rule found for recipient: [{}]", finalRecipient);
            }
            rulesPerRecipient.put(finalRecipient, rules);
        }


        for (WSBackendMessageType messageType : messageTypes) {
            for (Map.Entry<String, List<WSPluginDispatchRule>> rulesForRecipientEntry : rulesPerRecipient.entrySet()) {
                WSPluginDispatchRule rule = getRule(messageType, rulesForRecipientEntry);
                if (rule != null) {
                    String finalRecipient = rulesForRecipientEntry.getKey();
                    List<String> messageIdsForRecipient = messageIdsPerRecipient.get(finalRecipient);
                    retryService.send(messageIdsForRecipient, finalRecipient, rule, messageType);
                }
            }
        }
    }

    private WSPluginDispatchRule getRule(
            WSBackendMessageType messageType, Map.Entry<String,
            List<WSPluginDispatchRule>> stringListEntry) {
        for (WSPluginDispatchRule wsPluginDispatchRule : stringListEntry.getValue()) {
            for (WSBackendMessageType type : wsPluginDispatchRule.getTypes()) {
                if (type == messageType) {
                    return wsPluginDispatchRule;
                }
            }
        }
        return null;
    }

    private void addMessageIdToMap(String messageId, Map<String, List<String>> messageIdGroupedByRecipient) {
        String finalRecipient = userMessageExtService.getFinalRecipient(messageId);

        if (StringUtils.isBlank(finalRecipient)) {
            LOG.warn("No recipient found for messageId: [{}]", messageId);
            return;
        }
        List<String> messageIdsPerFinalRecipient = messageIdGroupedByRecipient.get(finalRecipient);
        if (isEmpty(messageIdsPerFinalRecipient)) {
            messageIdsPerFinalRecipient = new ArrayList<>();
        }
        messageIdsPerFinalRecipient.add(messageId);
        messageIdGroupedByRecipient.put(finalRecipient, messageIdsPerFinalRecipient);
    }
}
