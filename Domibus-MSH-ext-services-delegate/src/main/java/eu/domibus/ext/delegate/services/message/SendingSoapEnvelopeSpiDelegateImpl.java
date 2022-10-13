package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.SendingSoapEnvelopeSpiDelegate;
import eu.domibus.core.spi.soapenvelope.SendingSoapEnvelopeSpi;
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
public class SendingSoapEnvelopeSpiDelegateImpl implements SendingSoapEnvelopeSpiDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SendingSoapEnvelopeSpiDelegateImpl.class);

    protected SendingSoapEnvelopeSpi soapEnvelopeSpi;

    public SendingSoapEnvelopeSpiDelegateImpl(@Autowired(required = false) SendingSoapEnvelopeSpi soapEnvelopeSpi) {
        this.soapEnvelopeSpi = soapEnvelopeSpi;
    }

    @Override
    public SOAPMessage beforeSending(SOAPMessage soapMessage) {
        if (!isSoapEnvelopeSpiActive()) {
            LOG.debug("BeforeSending hook skipped: SPI is not active");
            return soapMessage;
        }

        LOG.debug("Executing beforeSending hook");
        final SOAPMessage resultSoapMessage = soapEnvelopeSpi.beforeSending(soapMessage);
        LOG.debug("Finished executing beforeSending hook");

        return resultSoapMessage;
    }

    protected boolean isSoapEnvelopeSpiActive() {
        return soapEnvelopeSpi != null;
    }
}
