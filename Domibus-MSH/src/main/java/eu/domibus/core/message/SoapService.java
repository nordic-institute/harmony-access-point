package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.core.ebms3.EbMS3Exception;
import org.apache.cxf.binding.soap.SoapMessage;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface SoapService {

    Ebms3Messaging getMessage(final SoapMessage message) throws IOException, JAXBException, EbMS3Exception;
}
