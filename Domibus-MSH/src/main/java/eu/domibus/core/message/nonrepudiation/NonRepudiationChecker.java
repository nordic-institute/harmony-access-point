package eu.domibus.core.message.nonrepudiation;

import eu.domibus.core.ebms3.EbMS3Exception;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface NonRepudiationChecker {

    List<String> getNonRepudiationDetailsFromSecurityInfoNode(Node securityInfo) throws EbMS3Exception;

    List<String> getNonRepudiationDetailsFromReceipt(Node nonRepudiationInformation) throws EbMS3Exception;

    boolean compareUnorderedReferenceNodeLists(List<String> referencesFromSecurityHeader, List<String> referencesFromNonRepudiationInformation);
}
