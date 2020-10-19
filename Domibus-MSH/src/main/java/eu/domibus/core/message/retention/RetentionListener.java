package eu.domibus.core.message.retention;

import com.google.gson.reflect.TypeToken;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private JsonUtil jsonUtil;

    @Timer(clazz = RetentionListener.class,value = "onMessage.deleteMessages")
    @Counter(clazz = RetentionListener.class,value = "onMessage.deleteMessages")
    public void onMessage(final Message message) {
        authUtils.runWithSecurityContext(() -> onMessagePrivate(message), "retention", "retention", AuthRole.ROLE_ADMIN);
    }

    protected void onMessagePrivate(final Message message) {

        try {
            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing JMS message for domain [{}]", domainCode);
            domainContextProvider.setCurrentDomain(domainCode);

            MessageDeleteType deleteType = MessageDeleteType.valueOf(message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE));
            if (MessageDeleteType.SINGLE == deleteType) {
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                LOG.debug("Delete one message [{}]", messageId);
                userMessageDefaultService.deleteMessage(messageId);
                return;
            }

            if (MessageDeleteType.MULTI == deleteType) {
                String userMessageLogsStr = message.getStringProperty(MessageRetentionDefaultService.MESSAGE_LOGS);

                List<UserMessageLog> userMessageLogs = deserializeMessageLog(userMessageLogsStr);

                LOG.info("There are [{}] messages to delete in batch", userMessageLogs.size());

                userMessageDefaultService.deleteMessages(userMessageLogs);
                return;
            }

            LOG.warn("Unknown message type [{}], JMS message will be ignored.", deleteType);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }

    protected List<UserMessageLog> deserializeMessageLog(String userMessageLogsStr) {
        Type type = new TypeToken<ArrayList<UserMessageLog>>() {
        }.getType();

        List<UserMessageLog> messageLogs = jsonUtil.jsonToListOfT(userMessageLogsStr, type);

        LOG.debug("UserMessageLogs size is [{}]", messageLogs.size());

        return messageLogs;
    }



}
