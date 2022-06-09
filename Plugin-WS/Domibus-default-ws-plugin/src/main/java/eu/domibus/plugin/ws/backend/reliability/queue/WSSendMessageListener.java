package eu.domibus.plugin.ws.backend.reliability.queue;

import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginMessageSender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import static eu.domibus.plugin.ws.backend.WSBackendMessageType.DELETED_BATCH;
import static eu.domibus.plugin.ws.backend.reliability.queue.WSSendMessageListener.WS_SEND_MESSAGE_LISTENER;

/**
 * WS Plugin Send Queue message listener
 *
 * @author François Gautier
 * @since 5.0
 */
@Service(WS_SEND_MESSAGE_LISTENER)
public class WSSendMessageListener implements MessageListener {

    public static final String WS_SEND_MESSAGE_LISTENER = "wsSendMessageListener";

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(WSSendMessageListener.class);

    public static final String ID = "ID";

    public static final String TYPE = "TYPE";

    private final WSPluginMessageSender wsPluginMessageSender;
    private final WSBackendMessageLogDao wsBackendMessageLogDao;
    private final DomainContextExtService domainContextExtService;
    private final AuthUtils authUtils;


    public WSSendMessageListener(WSPluginMessageSender wsPluginMessageSender,
                                 WSBackendMessageLogDao wsBackendMessageLogDao,
                                 DomainContextExtService domainContextExtService,
                                 AuthUtils authUtils) {
        this.wsPluginMessageSender = wsPluginMessageSender;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.domainContextExtService = domainContextExtService;
        this.authUtils = authUtils;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200)// 20 minutes
    @MDCKey(value = {IDomibusLogger.MDC_MESSAGE_ID, IDomibusLogger.MDC_MESSAGE_ENTITY_ID}, cleanOnStart = true)
    public void onMessage(Message message) {
        authUtils.runWithSecurityContext(()-> doOnMessage(message),
                "wsplugin_backend_notif", "wsplugin_backend_notif", AuthRole.ROLE_ADMIN);
    }

    protected void doOnMessage(Message message) {
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

        domainContextExtService.setCurrentDomain(new DomainDTO(domain, domain));
        LOG.debug("received message on wsPluginSendQueue for domain: [{}], backend message id [{}] and type [{}]", domain, id, type);

        WSBackendMessageLogEntity backendMessage = wsBackendMessageLogDao.getById(id);

        if (backendMessage == null) {
            LOG.error("Error while consuming JMS message: [{}] entity not found.", id);
            return;
        }

        putMDCDomibusId(backendMessage, messageId);

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

    private void putMDCDomibusId(WSBackendMessageLogEntity backendMessage, String messageId) {
        if(backendMessage.getType() == DELETED_BATCH){
            LOG.info("messageId: [{}]", messageId);
            return;
        }
        LOG.putMDC(IDomibusLogger.MDC_MESSAGE_ID, messageId);
    }

}
