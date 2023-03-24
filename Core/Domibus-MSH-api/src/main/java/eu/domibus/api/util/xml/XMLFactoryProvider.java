package eu.domibus.api.util.xml;

import eu.domibus.api.spring.SpringContextProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.TransformerFactory;

public class XMLFactoryProvider {

    public static TransformerFactory getTransformerFactory() {
        return getXmlUtil().getTransformerFactory();
    }

    public static MessageFactory getMessageFactory() {
        return getXmlUtil().getMessageFactorySoap12();
    }

    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        return getXmlUtil().getDocumentBuilderFactory();
    }

    public static DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware() {
        return getXmlUtil().getDocumentBuilderFactoryNamespaceAware();
    }

    private static XMLUtil getXmlUtil() {
        return SpringContextProvider.getApplicationContext().getBean(XMLUtil.BEAN_NAME, XMLUtil.class);
    }
}
