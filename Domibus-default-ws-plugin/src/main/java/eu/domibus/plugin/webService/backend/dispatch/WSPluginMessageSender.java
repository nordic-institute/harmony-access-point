package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;

/**
 * Common logic for sending messages to C1/C4 from WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
public abstract class WSPluginMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageSender.class);

    @Autowired
    protected WSPluginMessageBuilder wsPluginMessageBuilder;
    @Autowired
    protected WSPluginDispatcher wsPluginDispatcher;

    @Timer(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message")
    @Counter(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message")
    public void sendMessage(final WSMessageLogEntity wsMessageLogEntity) {
        String messageId = wsMessageLogEntity.getMessageId();
//


        try {
            final SOAPMessage requestSoapMessage = wsPluginMessageBuilder.buildSOAPMessageSendSuccess(messageId);
            wsPluginDispatcher.dispatch(requestSoapMessage, "http://localhost:8080/backend");
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            LOG.error("Error occurred when sending message with ID [{}]", messageId, t);
            throw t;
        } finally {
            try {
                LOG.debug("Finally handle reliability");
//                reliabilityService.handleReliability(messageId, messaging, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
            } catch (Exception ex) {
//                LOG.warn("Finally exception when handlingReliability", ex);
//                reliabilityService.handleReliabilityInNewTransaction(messageId, messaging, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
            }
        }
    }

}
