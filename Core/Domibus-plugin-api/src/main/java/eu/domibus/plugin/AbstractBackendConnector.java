package eu.domibus.plugin;

import eu.domibus.common.*;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.exception.TransformationException;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for implementing plugins
 *
 * @author Christian Koch, Stefan Mueller
 */
public abstract class AbstractBackendConnector<U, T> implements BackendConnector<U, T>, EnableAware {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractBackendConnector.class);

    protected final String name;
    protected List<NotificationType> requiredNotifications;

    @Autowired
    protected MessageRetriever messageRetriever;

    @Autowired
    protected MessageSubmitter messageSubmitter;

    @Autowired
    protected MessagePuller messagePuller;

    @Autowired
    protected MessageExtService messageExtService;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    DomainExtService domainExtService;

    public AbstractBackendConnector(final String name) {
        this.name = name;
    }

    @Override
    public String submit(final U message) throws MessagingProcessingException {
        try {
            final Submission messageData = getMessageSubmissionTransformer().transformToSubmission(message);
            final String messageId = messageSubmitter.submit(messageData, this.getName());
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
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
    @Transactional(propagation = Propagation.REQUIRED)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public T downloadMessage(final Long messageEntityId, final T target) throws MessageNotFoundException {
        LOG.debug("Downloading message [{}]", messageEntityId);
        if (messageEntityId != null) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID, String.valueOf(messageEntityId));
        }

        try {
            MessageStatus status = messageRetriever.getStatus(messageEntityId);
            if (MessageStatus.NOT_FOUND == status) {
                LOG.debug("Message with id [{}] was not found", messageEntityId);
                throw new MessageNotFoundException(String.format("Message with id [%s] was not found", messageEntityId));
            }
            if (MessageStatus.DOWNLOADED == status) {
                LOG.debug("Message with id [{}] was already downloaded", messageEntityId);
                throw new MessageNotFoundException(String.format("Message with id [%s] was already downloaded", messageEntityId));
            }

            Submission submission = messageRetriever.downloadMessage(messageEntityId);
            T t = this.getMessageRetrievalTransformer().transformFromSubmission(submission, target);

            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RETRIEVED);
            return t;
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public T downloadMessage(String messageId, T target) throws MessageNotFoundException {
        return downloadMessage(messageId, target, true);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void markMessageAsDownloaded(final String messageId) throws MessageNotFoundException {
        LOG.debug("Downloading message [{}]", messageId);
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }

        try {
            validateMessageStatus(messageId, true);
            messageRetriever.markMessageAsDownloaded(messageId);
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_CHANGED);
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public T downloadMessage(final String messageId, final T target, boolean markAsDownloaded) throws MessageNotFoundException {
        LOG.debug("Downloading message [{}]", messageId);
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }

        try {
            validateMessageStatus(messageId, markAsDownloaded);

            Submission submission = messageRetriever.downloadMessage(messageId, markAsDownloaded);
            T t = this.getMessageRetrievalTransformer().transformFromSubmission(submission, target);

            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RETRIEVED);
            return t;
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RETRIEVE_FAILED, ex);
            throw ex;
        }
    }

    private void validateMessageStatus(String messageId, boolean markAsDownloaded) throws MessageNotFoundException {
        MessageStatus status = messageRetriever.getStatus(messageId, MSHRole.RECEIVING);
        if (MessageStatus.NOT_FOUND == status) {
            LOG.debug("Message with id [{}] was not found", messageId);
            throw new MessageNotFoundException(String.format("Message with id [%s] was not found", messageId));
        }
        if (markAsDownloaded && MessageStatus.DOWNLOADED == status) {
            LOG.debug("Message with id [{}] was already downloaded", messageId);
            throw new MessageNotFoundException(String.format("Message with id [%s] was already downloaded", messageId));
        }
    }

    @Override
    public T browseMessage(String messageId, T target) throws MessageNotFoundException {
        LOG.debug("Browsing message [{}]", messageId);

        final Submission submission = messageRetriever.browseMessage(messageId);
        return this.getMessageRetrievalTransformer().transformFromSubmission(submission, target);
    }

    @Override
    public T browseMessage(String messageId, MSHRole mshRole, T target) throws MessageNotFoundException {
        LOG.debug("Browsing message [{}]-[{}]", messageId, mshRole);

        final Submission submission = messageRetriever.browseMessage(messageId, mshRole);
        return this.getMessageRetrievalTransformer().transformFromSubmission(submission, target);
    }

    @Override
    public T browseMessage(final Long messageEntityId, T target) throws MessageNotFoundException {
        LOG.debug("Browsing message [{}]", messageEntityId);

        final Submission submission = messageRetriever.browseMessage(messageEntityId);
        return this.getMessageRetrievalTransformer().transformFromSubmission(submission, target);
    }

    @Override
    public MessageStatus getStatus(final String messageId) throws MessageNotFoundException, DuplicateMessageException {
        return this.messageRetriever.getStatus(messageExtService.cleanMessageIdentifier(messageId));
    }

    @Override
    public MessageStatus getStatus(final String messageId, final MSHRole mshRole) throws MessageNotFoundException {
        return this.messageRetriever.getStatus(messageExtService.cleanMessageIdentifier(messageId), mshRole);
    }

    @Override
    public MessageStatus getStatus(final Long messageEntityId) throws MessageNotFoundException {
        return this.messageRetriever.getStatus(messageEntityId);
    }

    @Override
    public List<ErrorResult> getErrorsForMessage(final String messageId) throws MessageNotFoundException, DuplicateMessageException {
        List<ErrorResult> errorResults = new ArrayList<>();
        try{
            errorResults= new ArrayList<>(this.messageRetriever.getErrorsForMessage(messageId));
        } catch (MessageNotFoundException e) {
            LOG.error("Message [{}] does not exist", messageId);
        }
        return errorResults;
    }

    @Override
    public List<ErrorResult> getErrorsForMessage(final String messageId, final MSHRole mshRole) throws MessageNotFoundException {
        return new ArrayList<>(this.messageRetriever.getErrorsForMessage(messageId, mshRole));
    }

    @Override
    public void initiatePull(final String mpc) {
        messagePuller.initiatePull(mpc);
    }

    @Override
    public void messageReceiveFailed(final MessageReceiveFailureEvent event) {
        throw new UnsupportedOperationException("Plugins must implement this method");
    }

    @Override
    public void messageResponseSent(final MessageResponseSentEvent event) {
        // nothing to do by default
    }

    @Override
    public void messageStatusChanged(final MessageStatusChangeEvent event) {
        //this method should be implemented by the plugins needed to be notified when the User Message status changes
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
        throw new UnsupportedOperationException("Plugins must implement this method");
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent event) {
        throw new UnsupportedOperationException("Plugins must implement this method");
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        throw new UnsupportedOperationException("Plugins must implement this method");
    }

    @Override
    public void payloadSubmittedEvent(PayloadSubmittedEvent event) {
        //this method should be implemented by the plugins needed to be notified about payload submitted events
    }

    @Override
    public void payloadProcessedEvent(PayloadProcessedEvent event) {
        //this method should be implemented by the plugins needed to be notified about payload processed events
    }

    @Override
    public String getName() {
        return name;
    }

    public void setRequiredNotifications(List<NotificationType> requiredNotifications) {
        this.requiredNotifications = requiredNotifications;
    }

    @Override
    public List<NotificationType> getRequiredNotifications() {
        if (requiredNotifications != null) {
            LOG.trace("Using notifications [{}] from the plugin", requiredNotifications);
            return requiredNotifications;
        }
        List<NotificationType> defaultNotifications = BackendConnector.super.getRequiredNotifications();
        LOG.trace("Using default notifications [{}]", defaultNotifications);
        return defaultNotifications;
    }

    @Override
    public boolean isEnabled(final String domainCode) {
        LOG.debug("Should call doIsEnabled to support enabling and disabling the plugin on a domain");
        return true;
    }

    protected boolean doIsEnabled(String domainCode) {
        DomibusPropertyManagerExt propertyManager = getPropertyManager();
        String domainEnabledPropertyName = getDomainEnabledPropertyName();
        if (propertyManager != null) {
            String value = propertyManager.getKnownPropertyValue(domainCode, domainEnabledPropertyName);
            LOG.debug("Checking plugin property manager: reading property [{}]=[{}] to see if the plugin is enabled.", domainEnabledPropertyName, value);
            return BooleanUtils.toBoolean(value);
        }
        // fallback to the domibus property provider delegate
        DomainDTO domain = domainExtService.getDomain(domainCode);
        String value = domibusPropertyExtService.getProperty(domain, domainEnabledPropertyName);
        LOG.info("Checking domibus property manager: reading property [{}]=[{}] to see if the plugin is enabled.", domainEnabledPropertyName, value);
        return BooleanUtils.toBoolean(value);
    }

    @Override
    public void setEnabled(final String domainCode, final boolean enabled) {
        LOG.debug("Should call doSetEnabled to support enabling and disabling the plugin on a domain");
    }

    protected void doSetEnabled(String domainCode, boolean enabled) {
        String pluginName = getName();
        if (isEnabled(domainCode) == enabled) {
            LOG.info("Trying to set enabled as [{}] in plugin [{}] for domain [{}] but it is already so exiting;", enabled, pluginName, domainCode);
            return;
        }
        // just set the enabled property and the change listener will call domibus relevant methods
        DomibusPropertyManagerExt propertyManager = getPropertyManager();
        String domainEnabledPropertyName = getDomainEnabledPropertyName();
        if (propertyManager != null) {
            LOG.info("Calling plugin [{}] property manager: setting property [{}]=[{}] to enabled or disable plugin.", pluginName, domainEnabledPropertyName, enabled);
            propertyManager.setKnownPropertyValue(domainEnabledPropertyName, BooleanUtils.toStringTrueFalse(enabled));
            return;
        }
        // fallback to the domibus property provider delegate
        DomainDTO domain = domainExtService.getDomain(domainCode);
        LOG.info("Calling domibus [{}] property manager: setting property [{}]=[{}] to enabled or disable plugin.", pluginName, domainEnabledPropertyName, enabled);
        domibusPropertyExtService.setProperty(domain, domainEnabledPropertyName, BooleanUtils.toStringTrueFalse(enabled), true);
    }

    public void checkEnabled() {
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        if (!isEnabled(currentDomain.getCode())) {
            throw new DomibusServiceExtException(DomibusErrorCode.DOM_001, String.format("Plugin is disabled for domain [%s]", currentDomain));
        }
    }
}
