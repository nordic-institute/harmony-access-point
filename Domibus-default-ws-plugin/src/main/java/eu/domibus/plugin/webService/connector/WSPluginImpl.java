package eu.domibus.plugin.webService.connector;

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
import eu.domibus.plugin.webService.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import eu.domibus.plugin.webService.impl.StubDtoTransformer;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.plugin.webService.backend.WSBackendMessageType.*;

/**
 * Backend connector for the WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginImpl extends AbstractBackendConnector<Messaging, UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginImpl.class);

    public static final String PLUGIN_NAME = "backendWSPlugin";

    public static final String MESSAGE_SUBMISSION_FAILED = "Message submission failed";

    private final StubDtoTransformer defaultTransformer;

    protected WSMessageLogDao wsMessageLogDao;

    private final WSPluginBackendService wsPluginBackendService;

    public WSPluginImpl(StubDtoTransformer defaultTransformer,
                        WSMessageLogDao wsMessageLogDao,
                        WSPluginBackendService wsPluginBackendService) {
        super(PLUGIN_NAME);
        this.defaultTransformer = defaultTransformer;
        this.wsMessageLogDao = wsMessageLogDao;
        this.wsPluginBackendService = wsPluginBackendService;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
        LOG.info("Deliver message: [{}]", event);
        WSMessageLogEntity wsMessageLogEntity = new WSMessageLogEntity(
                event.getMessageId(),
                event.getProps().get(MessageConstants.FINAL_RECIPIENT),
                new Date());
        wsMessageLogDao.create(wsMessageLogEntity);

        wsPluginBackendService.send(event, RECEIVE_SUCCESS); //do not send the 2 notifications
        wsPluginBackendService.send(event, SUBMIT_MESSAGE);
    }

    @Override
    public void messageReceiveFailed(final MessageReceiveFailureEvent event) {
        LOG.info("Message receive failed [{}]", event);
        wsPluginBackendService.send(event, RECEIVE_FAIL);
    }

    @Override
    public void messageStatusChanged(final MessageStatusChangeEvent event) {
        LOG.info("Message status changed [{}]", event);
        wsPluginBackendService.send(event, MESSAGE_STATUS_CHANGE);
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        LOG.info("Message send failed [{}]", event);
        wsPluginBackendService.send(event, SEND_FAILURE);
    }

    @Override
    public void messageDeletedBatchEvent(final MessageDeletedBatchEvent event) {
        List<String> messageIds = event.getMessageDeletedEvents().stream().map(MessageDeletedEvent::getMessageId).collect(Collectors.toList());
        LOG.info("Message delete batch event [{}]", messageIds);
        wsMessageLogDao.deleteByMessageIds(messageIds);
        wsPluginBackendService.send(event, DELETED_BATCH);
    }

    @Override
    public void messageDeletedEvent(final MessageDeletedEvent event) {
        LOG.info("Message delete event [{}]", event.getMessageId());
        wsMessageLogDao.deleteByMessageId(event.getMessageId());
        wsPluginBackendService.send(event, DELETED);
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent event) {
        LOG.info("Message send success [{}]", event.getMessageId());
        wsPluginBackendService.send(event, SEND_SUCCESS);
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
