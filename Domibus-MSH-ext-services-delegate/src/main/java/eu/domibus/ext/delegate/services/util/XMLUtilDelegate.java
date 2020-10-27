package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.ext.services.XMLUtilExtService;

import javax.xml.soap.MessageFactory;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Delegate class allowing Domibus extension/plugin to use XML utility class of Domibus
 */
public class XMLUtilDelegate implements XMLUtilExtService {

    protected XMLUtil xmlUtil;

    public XMLUtilDelegate(XMLUtil xmlUtil) {
        this.xmlUtil = xmlUtil;
    }

    public MessageFactory getMessageFactorySoap12() {
        return xmlUtil.getMessageFactorySoap12();
    }
}
