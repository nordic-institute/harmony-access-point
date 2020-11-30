package eu.domibus.plugin.webService.backend.queue;

import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * WS Plugin Send Queue message listener
 *
 * @author François Gautier
 * @since 5.0
 */
@Service("wsSendMessageListener")
public class WSSendMessageListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSSendMessageListener.class);

    public static final String ID = "ID";

    public static final String TYPE = "TYPE";

    private final WSPluginMessageSender wsPluginMessageSender;
    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    public WSSendMessageListener(WSPluginMessageSender wsPluginMessageSender,
                                 WSBackendMessageLogDao wsBackendMessageLogDao) {
        this.wsPluginMessageSender = wsPluginMessageSender;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {AuthenticationExtException.class}, timeout = 1200)// 20 minutes
    @Override
    public void onMessage(Message message) {
        LOG.debug("received message on fsPluginSendQueue");

        String domain;
        String messageId;
        long id;
        String type;
        try {
            domain = message.getStringProperty(MessageConstants.DOMAIN);
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            id = message.getLongProperty(ID);
            type = message.getStringProperty(TYPE);
            LOG.debug("received message on wsPluginSendQueue for domain: [{}], domibus message id: [{}], backend message id [{}] and type [{}]", domain, messageId, id, type);
        } catch (JMSException e) {
            LOG.error("Unable to extract domainCode or fileName from JMS message");
            return;
        }

        WSBackendMessageLogEntity backendMessage = wsBackendMessageLogDao.getById(id);

        if(backendMessage == null){
            LOG.error("Error while consuming JMS message: [{}] entity not found.", id);
            return;
        }

        if (!StringUtils.equalsAnyIgnoreCase(messageId, backendMessage.getMessageId())) {
            LOG.error("Error while consuming JMS message: domibus message id incoherent [{}] =/= [{}]", messageId, backendMessage.getMessageId());
            return;
        }

        if (!StringUtils.equalsAnyIgnoreCase(type, backendMessage.getType().name())) {
            LOG.error("Error while consuming JMS message: type incoherent [{}] =/= [{}]", type, backendMessage.getType().name());
            return;
        }

        wsPluginMessageSender.sendNotification(backendMessage);
    }

}
