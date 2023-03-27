package eu.domibus.plugin.ws.connector;

import eu.domibus.common.*;
import eu.domibus.ext.domain.CronJobInfoDTO;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.exception.TransformationException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.initialize.PluginInitializer;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.ws.backend.reliability.queue.WSSendMessageListenerContainer;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.plugin.ws.initialize.WSPluginInitializer;
import eu.domibus.plugin.ws.message.WSMessageLogEntity;
import eu.domibus.plugin.ws.message.WSMessageLogService;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import eu.domibus.plugin.ws.webservice.StubDtoTransformer;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.plugin.ws.backend.WSBackendMessageType.*;
import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.DOMAIN_ENABLED;
import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.MESSAGE_NOTIFICATIONS;
import static eu.domibus.plugin.ws.property.listeners.WSPluginDispatcherCronExpressionChangeListener.SEND_RETRY_JOB_NAME;

/**
 * Backend connector for the WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginImpl extends AbstractBackendConnector<Messaging, UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginImpl.class);

    public static final String PLUGIN_NAME = "backendWSPlugin";

    private final StubDtoTransformer defaultTransformer;

    protected WSMessageLogService wsMessageLogService;

    private final WSPluginBackendService wsPluginBackendService;

    private final WSPluginPropertyManager wsPluginPropertyManager;

    private final WSSendMessageListenerContainer wsSendMessageListenerContainer;

    protected WSPluginInitializer wsPluginInitializer;

    public WSPluginImpl(StubDtoTransformer defaultTransformer,
                        WSMessageLogService wsMessageLogService,
                        WSPluginBackendService wsPluginBackendService,
                        WSPluginPropertyManager wsPluginPropertyManager,
                        WSSendMessageListenerContainer wsSendMessageListenerContainer,
                        DomibusPropertyExtService domibusPropertyExtService,
                        WSPluginInitializer wsPluginInitializer) {
        super(PLUGIN_NAME);
        this.defaultTransformer = defaultTransformer;
        this.wsMessageLogService = wsMessageLogService;
        this.wsPluginBackendService = wsPluginBackendService;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
        this.wsSendMessageListenerContainer = wsSendMessageListenerContainer;
        this.domibusPropertyExtService = domibusPropertyExtService;
        this.wsPluginInitializer = wsPluginInitializer;

        setRequiredNotifications();
    }

    @Override
    public boolean shouldCoreManageResources() {
        return true;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
        checkEnabled();

        LOG.info("Deliver message: [{}]", event);
        WSMessageLogEntity wsMessageLogEntity = new WSMessageLogEntity(
                event.getMessageId(),
                event.getProps().get(MessageConstants.CONVERSATION_ID),
                event.getProps().get(MessageConstants.REF_TO_MESSAGE_ID),
                event.getProps().get(MessageConstants.FROM_PARTY_ID),
                event.getProps().get(MessageConstants.FINAL_RECIPIENT),
                event.getProps().get(MessageConstants.ORIGINAL_SENDER),
                new Date());

        wsMessageLogService.create(wsMessageLogEntity);

        wsPluginBackendService.send(event, SUBMIT_MESSAGE);
        wsPluginBackendService.send(event, RECEIVE_SUCCESS);
    }

    /**
     * This method is a temporary method for the time of the old ws plugin lifecycle. It will remove the processing type set
     * per default as there is no processing type in the old plugin WSDL.
     * see EDELIVERY-8610
     */
    @Deprecated
    public String submitFromOldPlugin(final eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging message) throws MessagingProcessingException {
        checkEnabled();

        try {
            final Submission messageData = getMessageSubmissionTransformer().transformToSubmission(message);
            messageData.setProcessingType(null);
            final String messageId = this.messageSubmitter.submit(messageData, this.getName());
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SUBMITTED);
            return messageId;
        } catch (IllegalArgumentException iaEx) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SUBMIT_FAILED, iaEx);
            throw new TransformationException(iaEx);
        } catch (IllegalStateException ise) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SUBMIT_FAILED, ise);
            throw new PModeMismatchException(ise);
        } catch (MessagingProcessingException mpEx) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_SUBMIT_FAILED, mpEx);
            throw mpEx;
        }
    }

    @Override
    public void messageReceiveFailed(final MessageReceiveFailureEvent event) {
        checkEnabled();
        LOG.info("Message receive failed [{}]", event);
        wsPluginBackendService.send(event, RECEIVE_FAIL);
    }

    @Override
    public void messageStatusChanged(final MessageStatusChangeEvent event) {
        checkEnabled();
        LOG.info("Message status changed [{}]", event);
        wsPluginBackendService.send(event, MESSAGE_STATUS_CHANGE);
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        checkEnabled();
        LOG.info("Message send failed [{}]", event);
        wsPluginBackendService.send(event, SEND_FAILURE);
    }

    @Override
    public void messageDeletedBatchEvent(final MessageDeletedBatchEvent event) {
        checkEnabled();
        List<String> messageIds = event.getMessageDeletedEvents().stream().map(MessageDeletedEvent::getMessageId).collect(Collectors.toList());
        LOG.info("Message delete batch event [{}]", messageIds);
        wsMessageLogService.deleteByMessageIds(messageIds);
        wsPluginBackendService.send(event, DELETED_BATCH);
    }

    @Override
    public void messageDeletedEvent(final MessageDeletedEvent event) {
        checkEnabled();
        LOG.info("Message delete event [{}]", event.getMessageId());
        wsMessageLogService.deleteByMessageId(event.getMessageId());
        wsPluginBackendService.send(event, DELETED);
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent event) {
        checkEnabled();
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

    @Override
    public List<CronJobInfoDTO> getJobsInfo() {
        return Stream.of(SEND_RETRY_JOB_NAME)
                .map(CronJobInfoDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled(final String domainCode) {
        return doIsEnabled(domainCode);
    }

    @Override
    public void setEnabled(final String domainCode, final boolean enabled) {
        doSetEnabled(domainCode, enabled);
    }

    @Override
    public String getDomainEnabledPropertyName() {
        return DOMAIN_ENABLED;
    }

    @Override
    public DomibusPropertyManagerExt getPropertyManager() {
        return wsPluginPropertyManager;
    }

    @Override
    public PluginMessageListenerContainer getMessageListenerContainerFactory() {
        return wsSendMessageListenerContainer;
    }

    @Override
    public PluginInitializer getPluginInitializer() {
        return wsPluginInitializer;
    }

    public void setRequiredNotifications() {
        List<NotificationType> messageNotifications = domibusPropertyExtService.getConfiguredNotifications(MESSAGE_NOTIFICATIONS);
        LOG.debug("Using the following message notifications [{}]", messageNotifications);
        setRequiredNotifications(messageNotifications);
    }

}
