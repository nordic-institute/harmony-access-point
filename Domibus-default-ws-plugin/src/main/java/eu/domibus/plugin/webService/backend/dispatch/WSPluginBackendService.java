package eu.domibus.plugin.webService.backend.dispatch;

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

    public WSPluginBackendService(WSPluginBackendRetryService retryService,
                                  WSPluginDispatchRulesService wsBackendRulesService) {
        this.retryService = retryService;
        this.wsBackendRulesService = wsBackendRulesService;
    }

    public void sendNotification(WSBackendMessageType messageType, String messageId, String recipient) {
        if (StringUtils.isBlank(recipient)) {
            LOG.warn("No recipient found for messageId: [{}]", messageId);
            return;
        }

        List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(recipient);
        if (CollectionUtils.isEmpty(rules)) {
            LOG.warn("No rule found for recipient: [{}]", recipient);
            return;
        }

        for (WSPluginDispatchRule rule : rules) {
            if (rule.getTypes().contains(messageType)) {
                LOG.info("Rule [{}] found for message id [{}] and recipient [{}]", rule.getRuleName(), messageId, recipient);
                retryService.sendNotification(messageId, recipient, rule);
            }
        }
    }
}
