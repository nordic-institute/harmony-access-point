package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.ext.services.XMLUtilExtService;
import org.springframework.stereotype.Service;

import javax.xml.soap.MessageFactory;

/**
 * Delegate class allowing Domibus extension/plugin to use XML utility class of Domibus
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class XMLUtilDelegate implements XMLUtilExtService {

    protected XMLUtil xmlUtil;

    public XMLUtilDelegate(XMLUtil xmlUtil) {
        this.xmlUtil = xmlUtil;
    }

    public MessageFactory getMessageFactorySoap12() {
        return xmlUtil.getMessageFactorySoap12();
    }
}
