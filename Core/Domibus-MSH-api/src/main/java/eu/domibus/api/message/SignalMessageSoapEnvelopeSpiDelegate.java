package eu.domibus.api.message;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 */
public interface SignalMessageSoapEnvelopeSpiDelegate {

    /**
     * Hook point that can be used to validate or modify the SoapEnvelope before it is being sent to C3
     *
     * @param soapMessage that will be sent to C3 before signing/encryption
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
