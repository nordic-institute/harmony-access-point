package eu.domibus.core.ebms3.receiver.handler;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessagingServiceImpl;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.security.AuthorizationService;
import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Handles the incoming AS4 UserMessages
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingUserMessageHandler extends AbstractIncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingUserMessageHandler.class);

    @Autowired
    protected AttachmentCleanupService attachmentCleanupService;

    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    protected MetricRegistry metricRegistry;

    @Override
    @Timer(clazz = IncomingUserMessageHandler.class,value ="processMessage")
    @Counter(clazz = IncomingUserMessageHandler.class,value ="processMessage")
    protected SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        LOG.debug("Processing UserMessage");
        authorizationService.authorizeUserMessage(request, messaging.getUserMessage());
        com.codahale.metrics.Timer.Context timer = metricRegistry.timer(MetricRegistry.name(IncomingUserMessageHandler.class, "around_handleNewUserMessage.timer", "timer")).time();
        com.codahale.metrics.Counter counter = metricRegistry.counter(MetricRegistry.name(IncomingUserMessageHandler.class, "around_handleNewUserMessage.counter", "counter"));
        counter.inc();
        final SOAPMessage response = userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, request, messaging, testMessage);
        attachmentCleanupService.cleanAttachments(request);
        timer.stop();
        counter.dec();
        return response;
    }
}
