package eu.domibus.plugin.webService.backend.reliability.queue;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
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

import static eu.domibus.plugin.webService.backend.reliability.queue.WSSendMessageListener.WS_SEND_MESSAGE_LISTENER;

/**
 * WS Plugin Send Queue message listener
 *
 * @author François Gautier
 * @since 5.0
 */
@Service(WS_SEND_MESSAGE_LISTENER)
public class WSSendMessageListener implements MessageListener {

    public static final String WS_SEND_MESSAGE_LISTENER = "wsSendMessageListener";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSSendMessageListener.class);

    public static final String ID = "ID";

    public static final String TYPE = "TYPE";

    private final WSPluginMessageSender wsPluginMessageSender;
    private final WSBackendMessageLogDao wsBackendMessageLogDao;
    private final DomainContextExtService domainContextExtService;


    public WSSendMessageListener(WSPluginMessageSender wsPluginMessageSender,
                                 WSBackendMessageLogDao wsBackendMessageLogDao,
                                 DomainContextExtService domainContextExtService) {
        this.wsPluginMessageSender = wsPluginMessageSender;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.domainContextExtService = domainContextExtService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200)// 20 minutes
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_DOMAIN})
    public void onMessage(Message message) {
        String domain;
        String messageId;
        long id;
        String type;
        try {
            domain = message.getStringProperty(MessageConstants.DOMAIN);
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            id = message.getLongProperty(ID);
            type = message.getStringProperty(TYPE);
       } catch (JMSException e) {
            LOG.error("Unable to extract domainCode or fileName from JMS message", e);
            return;
        }

        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        domainContextExtService.setCurrentDomain(new DomainDTO(domain, domain));

        LOG.debug("received message on wsPluginSendQueue for domain: [{}], backend message id [{}] and type [{}]", domain, id, type);

        WSBackendMessageLogEntity backendMessage = wsBackendMessageLogDao.getById(id);

        if (backendMessage == null) {
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
        backendMessage.setScheduled(false);
    }

}
