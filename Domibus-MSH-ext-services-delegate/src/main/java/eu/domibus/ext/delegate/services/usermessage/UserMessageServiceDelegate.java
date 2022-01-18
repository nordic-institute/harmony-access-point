package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.ext.services.UserMessageExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
    public UserMessageDTO getMessage(String messageId) throws UserMessageExtException {
        LOG.debug("Getting message with messageId[{}].", messageId);
        userMessageSecurityService.checkMessageAuthorization(messageId);

        final UserMessage userMessage = userMessageCoreService.getMessage(messageId);
        if (userMessage == null) {
            return null;
        }
        return domibusExtMapper.userMessageToUserMessageDTO(userMessage);
    }

    @Override
    public String getUserMessageEnvelope(String messageId) {
        LOG.debug("Getting user message envelope with messageId [{}].", messageId);
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getUserMessageEnvelope(messageId);
    }

    @Override
    public String getSignalMessageEnvelope(String messageId) {
        LOG.debug("Getting user message envelope with messageId [{}].", messageId);
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getSignalMessageEnvelope(messageId);
    }

    @Override
    public String getFinalRecipient(String messageId) {
        LOG.debug("Getting message final recipient with messageId [{}].", messageId);
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getFinalRecipient(messageId);
    }

    @Override
    public String getOriginalSender(String messageId) {
        LOG.debug("Getting message final recipient with messageId [{}].", messageId);
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getOriginalSender(messageId);
    }


    @Override
    public void validateUserMessage(UserMessageDTO userMessage) throws UserMessageExtException {
        if (userMessageValidatorSpi == null) {
            throw new UserMessageExtException(DomibusErrorCode.DOM_001, "Could not validate: the validation SPI is not configured");
        }

        userMessageValidatorSpi.validateUserMessage(userMessage);
    }
}
