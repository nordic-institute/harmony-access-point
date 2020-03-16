package eu.domibus.mock;

import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service("NonRepudiationCheckerMock")
@Primary
public class NonRepudiationCheckerMock implements NonRepudiationChecker {

    @Override
    public List<String> getNonRepudiationDetailsFromSecurityInfoNode(Node securityInfo) throws EbMS3Exception {
        return null;
    }

    @Override
    public boolean compareUnorderedReferenceNodeLists(List<String> referencesFromSecurityHeader, List<String> referencesFromNonRepudiationInformation) {
        return true;
    }

    @Override
    public List<String> getNonRepudiationDetailsFromReceipt(Node nonRepudiationInformation) throws EbMS3Exception {
        return null;
    }
}
