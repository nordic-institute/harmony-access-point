package eu.domibus.core.nonrepudiation;

import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */

public interface NonRepudiationService {

    void saveRequest(String rawXMLMessage, UserMessage userMessage);

    String createNonRepudiation(SOAPMessage request) throws TransformerException;

    void saveResponse(String rawXMLMessage, SignalMessage signalMessage);


}
