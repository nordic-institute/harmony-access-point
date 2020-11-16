package eu.domibus.plugin.webService.backend;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
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

    final WSPluginMessageSender wsPluginMessageSender;
    final WSPluginDispatchRulesService wsBackendRulesService;

    public WSPluginBackendService(WSPluginMessageSender wsPluginMessageSender, WSPluginDispatchRulesService wsBackendRulesService) {
        this.wsPluginMessageSender = wsPluginMessageSender;
        this.wsBackendRulesService = wsBackendRulesService;
    }

    public void sendSuccess(String messageId, String recipient) {
        if (StringUtils.isBlank(recipient)) {
            LOG.debug("No recipient found for messageId: [{}]", messageId);
            return;
        }

        List<WSPluginDispatchRule> rules = wsBackendRulesService.getRulesByRecipient(recipient);
        if (CollectionUtils.isEmpty(rules)) {
            LOG.debug("No rule found for recipient: [{}]", recipient);
            return;
        }

        for (WSPluginDispatchRule wsPluginDispatchRule : rules) {
            if(wsPluginDispatchRule.getTypes().contains(WSBackendMessageType.SEND_SUCCESS)) {
                wsPluginMessageSender.sendMessageSuccess(getWsBackendMessageLogEntity(messageId, recipient, wsPluginDispatchRule));
            }
        }
    }

    protected WSBackendMessageLogEntity getWsBackendMessageLogEntity(String messageId, String recipient, WSPluginDispatchRule wsPluginDispatchRule) {
        WSBackendMessageLogEntity wsBackendMessageLogEntity = new WSBackendMessageLogEntity();
        wsBackendMessageLogEntity.setMessageId(messageId);
        wsBackendMessageLogEntity.setRuleName(wsPluginDispatchRule.getRuleName());
        wsBackendMessageLogEntity.setFinalRecipient(recipient);
        wsBackendMessageLogEntity.setType(WSBackendMessageType.SEND_SUCCESS);
        wsBackendMessageLogEntity.setSendAttempts(1);
        wsBackendMessageLogEntity.setSendAttemptsMax(wsPluginDispatchRule.getRetryCount());
        return wsBackendMessageLogEntity;
    }
}
