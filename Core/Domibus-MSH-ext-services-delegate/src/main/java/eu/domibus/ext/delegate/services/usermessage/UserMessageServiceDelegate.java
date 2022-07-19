package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.common.MSHRole;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
@Service
public class UserMessageServiceDelegate implements UserMessageExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageServiceDelegate.class);

    protected eu.domibus.api.usermessage.UserMessageService userMessageCoreService;
    protected DomibusExtMapper domibusExtMapper;
    protected UserMessageSecurityService userMessageSecurityService;
    protected UserMessageValidatorSpi userMessageValidatorSpi;

    public UserMessageServiceDelegate(UserMessageService userMessageCoreService,
                                      DomibusExtMapper domibusExtMapper,
                                      UserMessageSecurityService userMessageSecurityService,
                                      @Autowired(required = false) UserMessageValidatorSpi userMessageValidatorSpi) {
        this.userMessageCoreService = userMessageCoreService;
        this.domibusExtMapper = domibusExtMapper;
        this.userMessageSecurityService = userMessageSecurityService;
        this.userMessageValidatorSpi = userMessageValidatorSpi;
    }

    @Override
    public UserMessageDTO getMessage(String messageId) throws MessageNotFoundException {
        return getMessage(messageId, null);
    }

    @Override
    public UserMessageDTO getMessage(String messageId, MSHRole role) throws MessageNotFoundException {
        LOG.debug("Getting message with messageId[{}] and role[{}].", messageId, role);
        
        eu.domibus.api.model.MSHRole mshRole = role != null ? eu.domibus.api.model.MSHRole.valueOf(role.name()) : null;

        userMessageSecurityService.checkMessageAuthorization(messageId, mshRole);

        final UserMessage userMessage = userMessageCoreService.getMessage(messageId, mshRole);

        if (userMessage == null) {
            throw new MessageNotFoundException(String.format("Message [%s]-[%s] was not found", messageId, mshRole));
        }
        return domibusExtMapper.userMessageToUserMessageDTO(userMessage);
    }

    @Override
    public String getUserMessageEnvelope(String messageId) {
        return getUserMessageEnvelope(messageId, null);
    }

    @Override
    public String getUserMessageEnvelope(String messageId, MSHRole role) {
        LOG.debug("Getting user message envelope with messageId [{}].", messageId);
        eu.domibus.api.model.MSHRole mshRole = role != null ? eu.domibus.api.model.MSHRole.valueOf(role.name()) : null;

        userMessageSecurityService.checkMessageAuthorization(messageId, mshRole);

        return userMessageCoreService.getUserMessageEnvelope(messageId, mshRole);
    }

    @Override
    public String getSignalMessageEnvelope(String messageId) {
        return getSignalMessageEnvelope(messageId, null);
    }

    @Override
    public String getSignalMessageEnvelope(String messageId, MSHRole role) {
        LOG.debug("Getting user message envelope with messageId [{}].", messageId);
        eu.domibus.api.model.MSHRole mshRole = role != null ? eu.domibus.api.model.MSHRole.valueOf(role.name()) : null;

        userMessageSecurityService.checkMessageAuthorization(messageId, mshRole);

        return userMessageCoreService.getSignalMessageEnvelope(messageId, mshRole);
    }

    @Override
    public String getFinalRecipient(String messageId) {
        return getFinalRecipient(messageId, null);
    }

    @Override
    public String getFinalRecipient(String messageId, MSHRole role) {
        LOG.debug("Getting message final recipient with messageId [{}].", messageId);
        eu.domibus.api.model.MSHRole mshRole = role != null ? eu.domibus.api.model.MSHRole.valueOf(role.name()) : null;

        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, mshRole);

        return userMessageCoreService.getFinalRecipient(messageId, mshRole);
    }

    @Override
    public String getOriginalSender(String messageId) {
        return getOriginalSender(messageId, null);
    }

    @Override
    public String getOriginalSender(String messageId, MSHRole role) {
        LOG.debug("Getting message final recipient with messageId [{}].", messageId);
        eu.domibus.api.model.MSHRole mshRole = role != null ? eu.domibus.api.model.MSHRole.valueOf(role.name()) : null;

        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, mshRole);

        return userMessageCoreService.getOriginalSender(messageId, mshRole);
    }

    @Override
    public void validateUserMessage(UserMessageDTO userMessage) throws UserMessageExtException {
        if (userMessageValidatorSpi == null) {
            throw new UserMessageExtException(DomibusErrorCode.DOM_001, "Could not validate: the validation SPI is not configured");
        }

        userMessageValidatorSpi.validateUserMessage(userMessage);
    }
}
