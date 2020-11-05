package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;
import java.io.IOException;

/**
 * Common behaviour for handling incoming AS4 messages
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class AbstractIncomingMessageHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIncomingMessageHandler.class);

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected PModeProvider pModeProvider;

    @Override
    @Timer(clazz = AbstractIncomingMessageHandler.class,value = "processMessage")
    @Counter(clazz = AbstractIncomingMessageHandler.class,value = "processMessage")
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        SOAPMessage responseMessage = null;
        String pmodeKey = null;
        try {
            pmodeKey = (String) request.getProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
        } catch (final SOAPException soapEx) {
            //this error should never occur because pmode handling is done inside the in-interceptorchain
            LOG.error("Cannot find PModeKey property for incoming Message", soapEx);
            assert false;
        }
        Boolean testMessage = userMessageHandlerService.checkTestMessage(messaging.getUserMessage());
        LOG.info("Using pmodeKey {}", pmodeKey);
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
        try {
            responseMessage = processMessage(legConfiguration, pmodeKey, request, messaging, testMessage);
            LOG.businessInfo(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_RECEIVED : DomibusMessageCode.BUS_MESSAGE_RECEIVED,
                    messaging.getUserMessage().getFromFirstPartyId(), messaging.getUserMessage().getToFirstPartyId());

            LOG.debug("Ping message {}", testMessage);
        } catch (TransformerException | SOAPException | JAXBException | IOException e) {
            throw new UserMessageException(e);
        } catch (final EbMS3Exception e) {
            try {
                if (!testMessage && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) {
                    backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messaging.getUserMessage().getMessageInfo().getMessageId());
            }
            throw new WebServiceException(e);
        }
        return responseMessage;
    }

    protected abstract SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;
}
