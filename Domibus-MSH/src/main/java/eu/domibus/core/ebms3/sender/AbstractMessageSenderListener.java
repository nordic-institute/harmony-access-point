package eu.domibus.core.ebms3.sender;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class AbstractMessageSenderListener implements MessageListener {

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected MessageSenderService messageSenderService;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Override
    public void onMessage(final Message message) {
        String messageId = null;
        Long messageEntityId = null;

        int retryCount = 0;
        String domainCode = null;
        try {
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            if (message.propertyExists(MessageConstants.RETRY_COUNT)) {
                retryCount = message.getIntProperty(MessageConstants.RETRY_COUNT);
            }
            messageEntityId = Long.valueOf(message.getStringProperty(MessageConstants.MESSAGE_ENTITY_ID));
            domainCode = message.getStringProperty(MessageConstants.DOMAIN);
        } catch (final NumberFormatException nfe) {
            getLogger().trace("Error getting message properties", nfe);
            //This is ok, no delay has been set
        } catch (final JMSException e) {
            getLogger().error("Error processing JMS message", e);
        }
        if (StringUtils.isBlank(messageId)) {
            getLogger().error("Message ID is empty: could not send message");
            return;
        }
        if (messageEntityId == null) {
            getLogger().error("Message entity ID is empty: could not send message");
            return;
        }
        if (StringUtils.isBlank(domainCode)) {
            getLogger().error("Domain is empty: could not send message");
            return;
        }

        domainContextProvider.setCurrentDomain(domainCode);
        getLogger().putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        getLogger().debug("Sending message ID [{}] for domain [{}]", messageId, domainCode);

        sendUserMessage(messageId, messageEntityId, retryCount);

        getLogger().debug("Finished sending message ID [{}] for domain [{}]", messageId, domainCode);
    }

    public abstract DomibusLogger getLogger();

    public abstract void sendUserMessage(final String messageId, Long messageEntityId, int retryCount);
}
