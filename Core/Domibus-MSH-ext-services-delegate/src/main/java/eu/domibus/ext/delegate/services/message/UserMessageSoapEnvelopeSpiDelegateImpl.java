package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSoapEnvelopeSpiDelegate;
import eu.domibus.core.spi.soapenvelope.UserMessageSoapEnvelopeSpi;
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
public class UserMessageSoapEnvelopeSpiDelegateImpl implements UserMessageSoapEnvelopeSpiDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageSoapEnvelopeSpiDelegateImpl.class);

    protected UserMessageSoapEnvelopeSpi soapEnvelopeSpi;

    public UserMessageSoapEnvelopeSpiDelegateImpl(@Autowired(required = false) UserMessageSoapEnvelopeSpi soapEnvelopeSpi) {
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

    protected boolean isSoapEnvelopeSpiActive() {
        return soapEnvelopeSpi != null;
    }
}
