package eu.domibus.ebms3.receiver.handler;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.security.AuthorizationService;
import eu.domibus.ebms3.common.AttachmentCleanupService;
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
    private MetricRegistry metricRegistry;

    @Override
    protected SOAPMessage processMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        LOG.debug("Processing UserMessage");
        com.codahale.metrics.Timer.Context authorizeUserMessageMetric = metricRegistry.timer(MetricRegistry.name(IncomingUserMessageHandler.class, "authorizeUserMessage")).time();
        authorizationService.authorizeUserMessage(request, messaging.getUserMessage());
        authorizeUserMessageMetric.stop();
        com.codahale.metrics.Timer.Context handleNewUserMessageMetric = metricRegistry.timer(MetricRegistry.name(IncomingUserMessageHandler.class, "handleNewUserMessage")).time();
        final SOAPMessage response = userMessageHandlerService.handleNewUserMessage(legConfiguration, pmodeKey, request, messaging, testMessage);
        handleNewUserMessageMetric.stop();
        com.codahale.metrics.Timer.Context clean_attachement = null;
        try {
            clean_attachement = metricRegistry.timer(MetricRegistry.name(IncomingUserMessageHandler.class, "clean_attachement")).time();
            attachmentCleanupService.cleanAttachments(request);
            return response;
        } finally {
            if (clean_attachement != null) {
                clean_attachement.stop();
            }
        }
    }
}
