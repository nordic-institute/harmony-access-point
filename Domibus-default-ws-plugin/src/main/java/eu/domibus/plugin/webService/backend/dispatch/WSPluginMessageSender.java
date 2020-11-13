package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * Common logic for sending messages to C1/C4 from WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageSender.class);

    protected final WSPluginMessageBuilder wsPluginMessageBuilder;

    protected final WSPluginDispatcher wsPluginDispatcher;

    protected final WSPluginDispatchRulesService wsPluginDispatchRulesService;

    public WSPluginMessageSender(WSPluginMessageBuilder wsPluginMessageBuilder,
                                 WSPluginDispatcher wsPluginDispatcher,
                                 WSPluginDispatchRulesService wsPluginDispatchRulesService) {
        this.wsPluginMessageBuilder = wsPluginMessageBuilder;
        this.wsPluginDispatcher = wsPluginDispatcher;
        this.wsPluginDispatchRulesService = wsPluginDispatchRulesService;
    }

    @Timer(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message")
    @Counter(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message")
    public void sendMessageSuccess(final WSBackendMessageLogEntity wsBackendMessageLogEntity) {
        dispatch(wsBackendMessageLogEntity, wsPluginMessageBuilder.buildSOAPMessageSendSuccess(wsBackendMessageLogEntity));
    }

    private void dispatch(WSBackendMessageLogEntity wsBackendMessageLogEntity, SOAPMessage requestSoapMessage) {
        try {
            wsPluginDispatcher.dispatch(requestSoapMessage, wsPluginDispatchRulesService.getEndpoint(wsBackendMessageLogEntity.getRuleName()));
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            LOG.error("Error occurred when sending message with ID [{}]", wsBackendMessageLogEntity.getMessageId(), t);
            throw t;
        }
    }

}
