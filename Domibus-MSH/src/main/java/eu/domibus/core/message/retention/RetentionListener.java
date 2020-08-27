package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

/**
 * Listeners that deletes messages by their identifiers.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class RetentionListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionListener.class);

    @Autowired
    private UserMessageDefaultService userMessageDefaultService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retention", "retention", AuthRole.ROLE_ADMIN);
        }

        try {
            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing JMS message for domain [{}]", domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

            String deleteType = message.getStringProperty(MessageConstants.DELETE_TYPE);
            if (StringUtils.equals(DeleteType.DELETE_MESSAGE_ID_SINGLE.name(), deleteType)) {
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                userMessageDefaultService.deleteMessage(messageId);
                return;
            }

            if (StringUtils.equals(DeleteType.DELETE_MESSAGE_ID_MULTI.name(), deleteType)) {
                List<String> messageIds = (List<String>) message.getObjectProperty(MessageConstants.MESSAGE_IDS);
                userMessageDefaultService.deleteMessages(messageIds);
                return;
            }

            LOG.warn("Unknown message type [{}], JMS message will be ignored.", deleteType);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }

}
