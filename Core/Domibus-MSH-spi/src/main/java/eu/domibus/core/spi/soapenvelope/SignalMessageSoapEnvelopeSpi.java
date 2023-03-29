package eu.domibus.core.spi.soapenvelope;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 *
 * SPI interface gives the possibility to validate or modify the SoapEnvelope of the SignalMessage.
 */
public interface SignalMessageSoapEnvelopeSpi {

    /**
     * Hook point that can be used to validate or modify the SoapEnvelope before it is being sent to C2.
     * At this stage the signing and encryption are not yet applied in the SoapEnvelope.
     *
     * @param soapMessage that will be sent to C2 before signing/encryption
     * @return The modified SoapEnvelope or the same SoapEnvelope in case it has not been modified
     */
    SOAPMessage beforeSigningAndEncryption(SOAPMessage soapMessage);

    /**
     * Hook point that can be used to validate or modify the SoapEnvelope after it has been received from C3.
     *
     * @param responseMessage that is received from C3
     * @return The modified SoapEnvelope or the same SoapEnvelope in case it has not been modified
     */
    SOAPMessage afterReceiving(SOAPMessage responseMessage);
}