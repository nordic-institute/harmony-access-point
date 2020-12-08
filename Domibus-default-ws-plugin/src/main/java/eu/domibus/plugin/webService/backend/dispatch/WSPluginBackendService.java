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
import org.springframework.util.CollectionUtils;

import java.util.List;

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
        if (CollectionUtils.isEmpty(rules)) {
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
}
