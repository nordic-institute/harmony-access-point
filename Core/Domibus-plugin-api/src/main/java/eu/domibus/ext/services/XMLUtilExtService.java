package eu.domibus.ext.services;

import javax.xml.soap.MessageFactory;

/**
 * Interface allowing Domibus extension/plugin to use XML utility class of Domibus
 *
 * @since 5.0
 * @author François Gautier
 */
public interface XMLUtilExtService {

    /**
     * @return {@link MessageFactory} with {@link javax.xml.soap.SOAPConstants#SOAP_1_2_PROTOCOL}
     */
    MessageFactory getMessageFactorySoap12();
}
