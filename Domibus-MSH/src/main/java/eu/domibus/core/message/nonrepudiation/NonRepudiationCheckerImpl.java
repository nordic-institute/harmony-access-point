package eu.domibus.core.message.nonrepudiation;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


/**
 * @author feriaad on 15/02/2016.
 * @author Cosmin Baciu
 */
@Service
public class NonRepudiationCheckerImpl implements NonRepudiationChecker {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NonRepudiationCheckerImpl.class);
    public static final String REFERENCE = "Reference";
    public static final String MESSAGE_PART_NRINFORMATION = "MessagePartNRInformation";
    public static final String URI = "URI";
    public static final String DIGEST_VALUE = "DigestValue";

    @Override
    public List<String> getNonRepudiationDetailsFromSecurityInfoNode(final Node securityInfo) throws EbMS3Exception {
        List<String> result = new ArrayList<>();

        final NodeList childNodes = securityInfo.getChildNodes();
        if (childNodes == null || childNodes.getLength() == 0) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "No Reference Data found in SignedInfo node", null, null);
            e.setMshRole(MSHRole.SENDING);
            throw e;
        }

        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (REFERENCE.equals(item.getLocalName())) {
                addURIAndDigestValues(item, result);
            }
        }

        if (result.size() == 0) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "No Reference Data found in SignedInfo node", null, null);
            e.setMshRole(MSHRole.SENDING);
            throw e;
        }

        return result;
    }

    @Override
    public List<String> getNonRepudiationDetailsFromReceipt(Node nonRepudiationInformation) throws EbMS3Exception {
        List<String> result = new ArrayList<>();

        final NodeList childNodes = nonRepudiationInformation.getChildNodes();
        if (childNodes == null || childNodes.getLength() == 0) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "No Reference Data found in NonRepudiationInformation node", null, null);
            e.setMshRole(MSHRole.SENDING);
            throw e;
        }

        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (MESSAGE_PART_NRINFORMATION.equals(item.getLocalName())) {
                final Node referenceNode = getFirstChild(item, REFERENCE);
                addURIAndDigestValues(referenceNode, result);
            }
        }

        if (result.size() == 0) {
            EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, "No Reference Data found in NonRepudiationInformation node", null, null);
            e.setMshRole(MSHRole.SENDING);
            throw e;
        }

        return result;
    }

    protected Node getFirstChild(Node parent, String childName) {
        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (childName.equals(item.getLocalName())) {
                return item;
            }
        }

        return null;
    }

    protected String getReferenceURI(Node referenceNode) {
        final NamedNodeMap attributes = referenceNode.getAttributes();
        if (attributes == null) {
            return null;
        }
        final Node uri = attributes.getNamedItem(URI);
        if (uri == null) {
            return null;
        }
        return uri.getTextContent();
    }

    protected String getReferenceDigestValue(Node referenceNode) {
        final NodeList childNodes = referenceNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (DIGEST_VALUE.equals(item.getLocalName())) {
                return item.getTextContent();
            }
        }
        return null;
    }

    protected void addURIAndDigestValues(Node referenceNode, List<String> result) {
        if (referenceNode == null) {
            LOG.warn("Could not add URI or Digest Values: referenceNode is null");
            return;
        }

        final String referenceURI = getReferenceURI(referenceNode);
        if (StringUtils.isNotBlank(referenceURI)) {
            LOG.debug("Found URI attribute value [{}]", referenceURI);
            result.add(referenceURI);
        }

        final String referenceDigestValue = getReferenceDigestValue(referenceNode);
        if (StringUtils.isNotBlank(referenceDigestValue)) {
            LOG.debug("Found DigestValue [{}]", referenceDigestValue);
            result.add(referenceDigestValue);
        }
    }

    @Override
    public boolean compareUnorderedReferenceNodeLists(final List<String> referencesFromSecurityHeader, final List<String> referencesFromNonRepudiationInformation) {
        final int referencesFromSecurityHeaderSize = referencesFromSecurityHeader.size();
        final int referencesFromNonRepudiationInformationSize = referencesFromNonRepudiationInformation.size();
        if (referencesFromSecurityHeaderSize != referencesFromNonRepudiationInformationSize) {
            LOG.debug("References from the Request Security header size [{}] is different than references from the Response Security header [{}]", referencesFromSecurityHeaderSize, referencesFromNonRepudiationInformationSize);
            return false;
        }
        boolean found = false;
        for (int i = 0; i < referencesFromSecurityHeaderSize; ++i) {
            final String referenceFromSecurityHeader = referencesFromSecurityHeader.get(i);
            for (int j = 0; j < referencesFromNonRepudiationInformationSize; ++j) {
                if (referenceFromSecurityHeader.equals(referencesFromNonRepudiationInformation.get(j))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOG.error("The reference [{}] from the request Signature could not be found in the response NonRepudiationInformation", referenceFromSecurityHeader);
                return false;
            }
            found = false;
        }

        return true;
    }
}
