package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.model.PartInfo;
import eu.domibus.core.ebms3.EbMS3Exception;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0.1
 */
public interface UserMessagePayloadService {

    List<PartInfo> handlePayloads(SOAPMessage request, Ebms3Messaging ebms3Messaging, Ebms3MessageFragmentType ebms3MessageFragmentType)
            throws EbMS3Exception, SOAPException, TransformerException;

    void persistUpdatedPayloads(List<PartInfo> partInfos);
}
