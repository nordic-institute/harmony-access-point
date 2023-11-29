package eu.domibus.core.ebms3.receiver.handler;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.message.UserMessageErrorCreator;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
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
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Common behaviour for handling incoming AS4 messages
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class AbstractIncomingMessageHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractIncomingMessageHandler.class);

    private final static String INCOMING_TEST_MESSAGE ="incoming-test-message";

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    UserMessageErrorCreator userMessageErrorCreator;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    @Autowired
    protected MetricRegistry metricRegistry;

    @Override
    @Timer(clazz = AbstractIncomingMessageHandler.class, value = "processMessage")
    @Counter(clazz = AbstractIncomingMessageHandler.class, value = "processMessage")
    public SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging ebms3Messaging) {
        String pmodeKey = null;
        try {
            pmodeKey = (String) request.getProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
        } catch (final SOAPException soapEx) {
            //this error should never occur because pmode handling is done inside the in-interceptorchain
            LOG.error("Cannot find PModeKey property for incoming Message", soapEx);
            assert false;
        }
        final UserMessage userMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getUserMessage());
        boolean testMessage = userMessage.isTestMessage();
        if(testMessage) {
            String finalPmodeKey = pmodeKey;
            try {
                return metricRegistry.timer(name(AbstractIncomingMessageHandler.class, INCOMING_TEST_MESSAGE, "timer")).time(
                        () -> {
                            com.codahale.metrics.Counter testMessageCounter = null;
                            try {
                                testMessageCounter= metricRegistry.counter(name(AbstractIncomingMessageHandler.class, INCOMING_TEST_MESSAGE, "counter"));
                                testMessageCounter.inc();
                                return processMessage(request, ebms3Messaging, finalPmodeKey, userMessage, true);
                            } finally {
                                Optional.ofNullable(testMessageCounter).ifPresent(com.codahale.metrics.Counter::dec);
                            }
                        }
                );
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e){
                new WebServiceException("Unexpected exception", e);    //should never happen
            }
        }
        return processMessage(request, ebms3Messaging, pmodeKey, userMessage, false);
    }

    private SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging ebms3Messaging, String pmodeKey, UserMessage userMessage, Boolean testMessage) {
        SOAPMessage responseMessage;
        LOG.info("Using pmodeKey {}", pmodeKey);
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pmodeKey);
        try {
            responseMessage = processMessage(legConfiguration, pmodeKey, request, ebms3Messaging, testMessage);
            LOG.businessInfo(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_RECEIVED : DomibusMessageCode.BUS_MESSAGE_RECEIVED,
                    ebms3Messaging.getUserMessage().getFromFirstPartyId(), ebms3Messaging.getUserMessage().getToFirstPartyId());

            LOG.debug("Ping message {}", testMessage);
        } catch (TransformerException | SOAPException | JAXBException | IOException e) {
            throw new UserMessageException(e);
        } catch (final EbMS3Exception e) {
            try {
                if (legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) {
                    backendNotificationService.notifyMessageReceivedFailure(userMessage, userMessageErrorCreator.createErrorResult(e));
                }
            } catch (Exception ex) {
                LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, ebms3Messaging.getUserMessage().getMessageInfo().getMessageId());
            }
            throw new WebServiceException(e);
        }
        return responseMessage;
    }

    protected abstract SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Ebms3Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;
}
