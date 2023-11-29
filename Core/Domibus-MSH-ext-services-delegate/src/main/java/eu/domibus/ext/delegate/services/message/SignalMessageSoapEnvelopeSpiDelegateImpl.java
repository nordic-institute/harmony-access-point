package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.SignalMessageSoapEnvelopeSpiDelegate;
import eu.domibus.core.spi.soapenvelope.SignalMessageSoapEnvelopeSpi;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 *
 */
@Service
public class SignalMessageSoapEnvelopeSpiDelegateImpl implements SignalMessageSoapEnvelopeSpiDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalMessageSoapEnvelopeSpiDelegateImpl.class);

    protected SignalMessageSoapEnvelopeSpi soapEnvelopeSpi;

    public SignalMessageSoapEnvelopeSpiDelegateImpl(@Autowired(required = false) SignalMessageSoapEnvelopeSpi soapEnvelopeSpi) {
        this.soapEnvelopeSpi = soapEnvelopeSpi;
    }

    @Override
    public SOAPMessage beforeSigningAndEncryption(SOAPMessage soapMessage) {
        if (!isSoapEnvelopeSpiActive()) {
            LOG.debug("BeforeSigningAndEncryption hook skipped: SPI is not active");
            return soapMessage;
        }

        LOG.debug("Executing beforeSigningAndEncryption hook");
        final SOAPMessage resultSoapMessage = soapEnvelopeSpi.beforeSigningAndEncryption(soapMessage);
        LOG.debug("Finished executing beforeSigningAndEncryption hook");

        return resultSoapMessage;
    }

    @Override
    public SOAPMessage afterReceiving(SOAPMessage responseMessage) {
        if (!isSoapEnvelopeSpiActive()) {
            LOG.debug("afterReceiving hook skipped: SPI is not active");
            return responseMessage;
        }

        LOG.debug("Executing afterReceiving hook");
        final SOAPMessage resultSoapMessage = soapEnvelopeSpi.afterReceiving(responseMessage);
        LOG.debug("Finished executing afterReceiving hook");

        return resultSoapMessage;
    }

    protected boolean isSoapEnvelopeSpiActive() {
        return soapEnvelopeSpi != null;
    }
}
