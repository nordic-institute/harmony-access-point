package eu.domibus.plugin.ws.client;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Log all soap interactions
 */
public class MessageLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageLoggingHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return new HashSet<>();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        boolean isRequest = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (isRequest) {
            LOG.info("======== Logging Request ========");
        } else {
            LOG.info("======== Logging Response ========");
        }

        logSOAPMessage(context.getMessage());

        //continue with message processing
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {

        LOG.info("======== Logging SOAPFault ========");
        logSOAPMessage(context.getMessage());

        return true;
    }

    @Override
    public void close(MessageContext context) {
        //do nothing
    }

    private void logSOAPMessage(SOAPMessage message) {
        String output = null;

        try {
            output = convertToString(message);
        } catch (IOException | SOAPException e) {
            LOG.error("", e);
        }

        if(output != null) {
            LOG.info(output);
        }
    }

    private String convertToString(SOAPMessage message) throws IOException, SOAPException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        message.writeTo(stream);

        return new String(stream.toByteArray(), StandardCharsets.UTF_8);
    }
}
