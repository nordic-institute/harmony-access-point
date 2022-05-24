package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageStatus;
import eu.domibus.plugin.ws.backend.WSBackendMessageType;
import eu.domibus.plugin.ws.backend.reliability.WSPluginBackendReliabilityService;
import eu.domibus.plugin.ws.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.ws.backend.rules.WSPluginDispatchRulesService;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.exception.WSPluginException;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.slf4j.Slf4jEventSender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Common logic for sending messages to C1/C4 from WS Plugin
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageSender extends Slf4jEventSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageSender.class);

    protected final WSPluginBackendReliabilityService reliabilityService;

    protected final WSBackendMessageLogDao wsBackendMessageLogDao;

    protected final WSPluginDispatchRulesService rulesService;

    protected final WSPluginMessageBuilder messageBuilder;

    protected final WSPluginDispatcher dispatcher;

    protected final WSPluginImpl wsPlugin;

    @Autowired
    XMLUtil xmlUtil;

    static final String ORG_APACHE_CXF_CATEGORY = "org.apache.cxf";

    public WSPluginMessageSender(WSPluginBackendReliabilityService reliabilityService,
                                 WSBackendMessageLogDao wsBackendMessageLogDao,
                                 WSPluginDispatchRulesService rulesService,
                                 WSPluginMessageBuilder messageBuilder,
                                 WSPluginDispatcher dispatcher,
                                 WSPluginImpl wsPlugin) {
        this.reliabilityService = reliabilityService;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.rulesService = rulesService;
        this.messageBuilder = messageBuilder;
        this.dispatcher = dispatcher;
        this.wsPlugin = wsPlugin;
    }

    /**
     * Send notification to the backend service with reliability feature
     *
     * @param backendMessage persisted message
     */
    @Timer(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    @Counter(clazz = WSPluginMessageSender.class, value = "wsplugin_outgoing_backend_message_notification")
    public void sendNotification(final WSBackendMessageLogEntity backendMessage) {
        LOG.debug("Rule [{}] Send notification backend notification [{}] for backend message id [{}]",
                backendMessage.getRuleName(),
                backendMessage.getType(),
                backendMessage.getEntityId());
        WSPluginDispatchRule dispatchRule = null;
        SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(backendMessage);
        try {
            dispatchRule = rulesService.getRule(backendMessage.getRuleName());
            String endpoint = dispatchRule.getEndpoint();
            LOG.debug("Endpoint identified: [{}]", endpoint);
            SOAPMessage soapSent = dispatcher.dispatch(soapMessage, endpoint);
            backendMessage.setBackendMessageStatus(WSBackendMessageStatus.SENT);
            LOG.info("Backend notification [{}] for domibus id [{}] sent to [{}] successfully",
                    backendMessage.getType(),
                    backendMessage.getMessageId(),
                    endpoint);
            String rawSoapXml = getRawXMLMessage(soapMessage);

            if (isCxfLoggingInfoEnabled()){
                LOG.info("The soap message push notification sent to C4 for message with id [{}] is: [{}]",
                        backendMessage.getMessageId(), getRawXMLMessage(soapSent));
                LOG.info("The soap message received from C4 for id [{}] is: [{}]", backendMessage.getMessageId(), rawSoapXml);

            }

            if (backendMessage.getType() == WSBackendMessageType.SUBMIT_MESSAGE) {
                wsPlugin.downloadMessage(backendMessage.getMessageId(), null);
            }
        } catch (Throwable t) {//NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions.
            if (dispatchRule == null) {
                throw new WSPluginException("No dispatch rule found for backend message id [" + backendMessage.getEntityId() + "]");
            }
            reliabilityService.handleReliability(backendMessage, dispatchRule);
            LOG.error("Error occurred when sending backend message with ID [{}]", backendMessage.getEntityId(), t);
        }
    }



    protected boolean isCxfLoggingInfoEnabled() {
        boolean isCxfLoggingInfoEnabled = LoggerFactory.getLogger(ORG_APACHE_CXF_CATEGORY).isInfoEnabled();
        LOG.debug("[{}] is {}set to INFO level", ORG_APACHE_CXF_CATEGORY, isCxfLoggingInfoEnabled ? StringUtils.EMPTY : "not ");
        return isCxfLoggingInfoEnabled;
    }


    private String getRawXmlFromNode(Node node) throws TransformerException {
        final StringWriter rawXmlMessageWriter = new StringWriter();

        TransformerFactory transformerFactory = xmlUtil.getTransformerFactory();

        transformerFactory.newTransformer().transform(
                new DOMSource(node),
                new StreamResult(rawXmlMessageWriter));

        return rawXmlMessageWriter.toString();
    }

    private String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        return getRawXmlFromNode(soapMessage.getSOAPPart());
    }

}
