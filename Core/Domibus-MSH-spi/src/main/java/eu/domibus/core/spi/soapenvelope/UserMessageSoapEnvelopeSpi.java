package eu.domibus.core.spi.soapenvelope;

import org.apache.cxf.message.Attachment;

import javax.xml.soap.SOAPMessage;
import java.util.Collection;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 *
 * SPI interface gives the possibility to validate or modify the SoapEnvelope of the UserMessage.
 */
public interface UserMessageSoapEnvelopeSpi {

    /**
     * Hook point that can be used to validate or modify the SoapEnvelope before it is being sent to C3.
     * At this stage the signing and encryption are not yet applied in the SoapEnvelope.
     *
     * @param soapMessage that will be sent to C3 before signing/encryption
     * @return The modified SoapEnvelope or the same SoapEnvelope in case it has not been modified
     */
    SOAPMessage beforeSigningAndEncryption(SOAPMessage soapMessage);

    /**
     * Hook point that can be used to modify the SoapEnvelope after it has been signed and encrypted before it is being sent to C2.
     *
     * @param soapMessage that will be sent to C2 after signing/encryption
     */
    void afterSigningAndEncryption(SOAPMessage soapMessage, Collection<Attachment> attachments);
}
