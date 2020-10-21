package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.UserMessageDTO;
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

    @Autowired
    eu.domibus.api.usermessage.UserMessageService userMessageCoreService;

    @Autowired
    DomainExtConverter domainConverter;

    @Autowired
    UserMessageSecurityService userMessageSecurityService;

    @Override
    public UserMessageDTO getMessage(String messageId) throws UserMessageExtException {
        LOG.debug("Getting message with messageId='" + messageId + "'");
        userMessageSecurityService.checkMessageAuthorization(messageId);

        final UserMessage userMessage = userMessageCoreService.getMessage(messageId);
        if (userMessage == null) {
            return null;
        }
        return domainConverter.convert(userMessage, UserMessageDTO.class);
    }

    @Override
    public String getUserMessageEnvelope(String messageId) {
        LOG.debug("Getting user message envelope with messageId='" + messageId + "'");
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getUserMessageEnvelope(messageId);
    }

    @Override
    public String getSignalMessageEnvelope(String messageId) {
        LOG.debug("Getting user message envelope with messageId='" + messageId + "'");
        userMessageSecurityService.checkMessageAuthorization(messageId);

        return userMessageCoreService.getSignalMessageEnvelope(messageId);
    }

//    @Override
//    public String getMessageEnvelope(String messageId, String messageType) {
//        LOG.debug("Getting message envelope with messageId='" + messageId + "'");
//        userMessageSecurityService.checkMessageAuthorization(messageId);
//
//        final String xml = userMessageCoreService.getMessageEnvelope(messageId, messageType);
//        return xml;
//    }
}
