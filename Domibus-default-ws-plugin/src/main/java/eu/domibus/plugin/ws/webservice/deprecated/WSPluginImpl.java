package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.common.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.ws.webservice.WSMessageLogDao;
import eu.domibus.plugin.ws.webservice.WSMessageLogEntity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backend connector for the WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 * @deprecated since 5.0 Use instead {@link eu.domibus.plugin.ws.connector.WSPluginImpl}
 */
public class WSPluginImpl extends AbstractBackendConnector<Messaging, UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginImpl.class);

    public static final String DEPRECATED_PLUGIN_NAME = "backendWSPluginDeprecated";

    public static final String MESSAGE_SUBMISSION_FAILED = "Message submission failed";

    private final StubDtoTransformer defaultTransformer;

    protected WSMessageLogDao wsMessageLogDao;

    public WSPluginImpl(StubDtoTransformer defaultTransformer,
                        WSMessageLogDao wsMessageLogDao) {
        super(DEPRECATED_PLUGIN_NAME);
        this.defaultTransformer = defaultTransformer;
        this.wsMessageLogDao = wsMessageLogDao;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
        LOG.info("[DEPRECATED] Deliver message: [{}]", event);
        WSMessageLogEntity wsMessageLogEntity = new WSMessageLogEntity(
                event.getMessageId(),
                event.getProps().get(MessageConstants.FINAL_RECIPIENT),
                new Date());
        wsMessageLogDao.create(wsMessageLogEntity);
    }

    @Override
    public void messageReceiveFailed(final MessageReceiveFailureEvent event) {
        LOG.info("[DEPRECATED] Message receive failed [{}]", event);
    }

    @Override
    public void messageStatusChanged(final MessageStatusChangeEvent event) {
        LOG.info("[DEPRECATED] Message status changed [{}]", event);
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        LOG.info("[DEPRECATED] Message send failed [{}]", event);
    }

    @Override
    public void messageDeletedBatchEvent(final MessageDeletedBatchEvent event) {
        List<String> messageIds = event.getMessageDeletedEvents().stream().map(MessageDeletedEvent::getMessageId).collect(Collectors.toList());
        LOG.info("[DEPRECATED] Message delete batch event [{}]", messageIds);
        wsMessageLogDao.deleteByMessageIds(messageIds);
    }

    @Override
    public void messageDeletedEvent(final MessageDeletedEvent event) {
        LOG.info("[DEPRECATED] Message delete event [{}]", event.getMessageId());
        wsMessageLogDao.deleteByMessageId(event.getMessageId());
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent event) {
        LOG.info("[DEPRECATED] Message send success [{}]", event.getMessageId());
    }

    @Override
    public MessageSubmissionTransformer<Messaging> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    @Override
    public MessageRetrievalTransformer<UserMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
    }

    public MessageRetriever getMessageRetriever() {
        return this.messageRetriever;
    }
}
