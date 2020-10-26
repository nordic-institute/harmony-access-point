package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.ext.services.XMLUtilExtService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.MessageFactory;

/**
 * @author François Gautier
 * @since 5.0
 */
public class XMLUtilDelegate implements XMLUtilExtService {

    @Autowired
    protected XMLUtil xmlUtil;

    public MessageFactory getMessageFactorySoap12() {
        return xmlUtil.getMessageFactorySoap12();
    }
}
