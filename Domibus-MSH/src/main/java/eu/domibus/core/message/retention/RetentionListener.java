package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR;

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

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMessage(final Message message) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retention", "retention", AuthRole.ROLE_ADMIN);
        }

        try {
            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing JMS message for domain [{}]", domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

            MessageDeleteType deleteType = MessageDeleteType.valueOf(message.getStringProperty(MessageRetentionService.DELETE_TYPE));
            if (MessageDeleteType.DELETE_MESSAGE_ID_SINGLE == deleteType) {
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                LOG.debug("Delete one message [{}]", messageId);
                userMessageDefaultService.deleteMessage(messageId);
                return;
            }

            if (MessageDeleteType.DELETE_MESSAGE_ID_MULTI == deleteType) {
                final String separator = domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR);
                List<String> messageIds = Arrays.asList(StringUtils.splitByWholeSeparator(message.getStringProperty(MessageRetentionService.MESSAGE_IDS), separator));
                LOG.debug("There are [{}] messages to delete [{}] in batch", messageIds.size(), messageIds);
                userMessageDefaultService.deleteMessages(messageIds);
                return;
            }

            LOG.warn("Unknown message type [{}], JMS message will be ignored.", deleteType);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }
}
