package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageStatus;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.WSPluginBackendReliabilityService;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import eu.domibus.plugin.webService.connector.WSPluginImpl;
import eu.domibus.plugin.webService.exception.WSPluginException;
import org.springframework.stereotype.Service;

/**
 * Common logic for sending messages to C1/C4 from WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageSender.class);

    protected final WSPluginBackendReliabilityService reliabilityService;

    protected final WSBackendMessageLogDao wsBackendMessageLogDao;

    protected final WSPluginDispatchRulesService rulesService;

    protected final WSPluginMessageBuilder messageBuilder;

    protected final WSPluginDispatcher dispatcher;

    protected final WSPluginImpl wsPlugin;

    public WSPluginMessageSender(WSPluginBackendReliabilityService reliabilityService,
                                 WSBackendMessageLogDao wsBackendMessageLogDao,
                                 WSPluginDispatchRulesService rulesService,
                                 WSPluginMessageBuilder messageBuilder,
                                 WSPluginDispatcher dispatcher,
                                 WSPluginImpl wsPlugin) {
        this.reliabilityService = reliabilityService;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.rulesService = rulesService;
        this.messageBuilder = messageBuilder;
        this.dispatcher = dispatcher;
        this.wsPlugin = wsPlugin;
    }

    /**
     * Send notification to the backend service with reliability feature
     *
     * @param backendMessage persisted message
     */
    @Timer(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    @Counter(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    public void sendNotification(final WSBackendMessageLogEntity backendMessage) {
        LOG.debug("Rule [{}] Send notification backend notification [{}] for backend message id [{}]",
                backendMessage.getRuleName(),
                backendMessage.getType(),
                backendMessage.getEntityId());
        WSPluginDispatchRule dispatchRule = null;
        try {
            backendMessage.setMessageStatus(WSBackendMessageStatus.SEND_IN_PROGRESS); // to be deleted
            dispatchRule = rulesService.getRule(backendMessage.getRuleName());
            String endpoint = dispatchRule.getEndpoint();
            LOG.debug("Endpoint identified: [{}]", endpoint);
            dispatcher.dispatch(messageBuilder.buildSOAPMessage(backendMessage), endpoint);
            backendMessage.setMessageStatus(WSBackendMessageStatus.SENT);
            LOG.info("Backend notification [{}] for domibus id [{}] sent to [{}] successfully",
                    backendMessage.getType(),
                    backendMessage.getMessageId(),
                    endpoint);
            if (backendMessage.getType() == WSBackendMessageType.SUBMIT_MESSAGE) {
                wsPlugin.downloadMessage(backendMessage.getMessageId(), null);
            }
        } catch (Throwable t) {//NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions.
            if (dispatchRule == null) {
                throw new WSPluginException("No dispatch rule found for backend message id [" + backendMessage.getEntityId() + "]");
            }
            reliabilityService.handleReliability(backendMessage, dispatchRule);
            LOG.error("Error occurred when sending backend message with ID [{}]", backendMessage.getEntityId(), t);
        }
    }

}
