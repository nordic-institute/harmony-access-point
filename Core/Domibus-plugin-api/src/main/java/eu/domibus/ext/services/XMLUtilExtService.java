package eu.domibus.ext.services;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.TransformerFactory;

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

    /**
     * @return {@link TransformerFactory} with {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} true
     */
    TransformerFactory getTransformerFactory();

    DocumentBuilderFactory getDocumentBuilderFactory();

    DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware();
}
