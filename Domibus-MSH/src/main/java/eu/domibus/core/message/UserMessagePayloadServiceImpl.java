package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.model.Description;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.dictionary.PartPropertyDictionaryService;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.cxf.attachment.AttachmentUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
public class UserMessagePayloadServiceImpl implements UserMessagePayloadService {

    private static final String HASH_SIGN = "#";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageHandlerServiceImpl.class);

    protected SoapUtil soapUtil;
    protected XMLUtil xmlUtil;
    protected PartPropertyDictionaryService partPropertyDictionaryService;
    protected PartInfoDao partInfoDao;

    public UserMessagePayloadServiceImpl(SoapUtil soapUtil, XMLUtil xmlUtil, PartPropertyDictionaryService partPropertyDictionaryService, PartInfoDao partInfoDao) {
        this.soapUtil = soapUtil;
        this.xmlUtil = xmlUtil;
        this.partPropertyDictionaryService = partPropertyDictionaryService;
        this.partInfoDao = partInfoDao;
    }

    @Override
    public List<PartInfo> handlePayloads(SOAPMessage request, Ebms3Messaging ebms3Messaging, Ebms3MessageFragmentType ebms3MessageFragmentType)
            throws EbMS3Exception, SOAPException, TransformerException {
        LOG.debug("Start handling payloads");

        final String messageId = ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();

        List<PartInfo> partInfoList = getPartInfoList(ebms3Messaging);
        if (ebms3MessageFragmentType != null) {
            final PartInfo partInfoFromFragment = getPartInfoFromFragment(ebms3MessageFragmentType);
            partInfoList.add(partInfoFromFragment);
        }

        boolean bodyloadFound = false;
        for (final PartInfo partInfo : partInfoList) {
            final String cid = partInfo.getHref();
            LOG.debug("looking for attachment with cid: {}", cid);
            boolean payloadFound = false;
            if (isBodyloadCid(cid)) {
                if (bodyloadFound) {
                    LOG.businessError(DomibusMessageCode.BUS_MULTIPLE_PART_INFO_REFERENCING_SOAP_BODY);
                    throw EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                            .message("More than one Partinfo referencing the soap body found")
                            .refToMessageId(messageId)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                }
                LOG.info("Using soap body payload");
                bodyloadFound = true;
                payloadFound = true;
                Node bodyContent = soapUtil.getChildElement(request);
                LOG.debug("Soap BodyContent when handling payloads: [{}]", bodyContent);

                partInfo.setPayloadDatahandler(getDataHandler(bodyContent));
                partInfo.setInBody(true);
            }
            @SuppressWarnings("unchecked") final Iterator<AttachmentPart> attachmentIterator = request.getAttachments();
            AttachmentPart attachmentPart;
            while (attachmentIterator.hasNext() && !payloadFound) {

                attachmentPart = attachmentIterator.next();
                //remove square brackets from cid for further processing
                attachmentPart.setContentId(AttachmentUtil.cleanContentId(attachmentPart.getContentId()));
                LOG.debug("comparing with: " + attachmentPart.getContentId());
                if (attachmentPart.getContentId().equals(AttachmentUtil.cleanContentId(cid))) {
                    partInfo.setPayloadDatahandler(attachmentPart.getDataHandler());
                    partInfo.setInBody(false);
                    payloadFound = true;
                }
            }
            if (!payloadFound) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_ATTACHMENT_NOT_FOUND, cid);
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0011)
                        .message("No Attachment found for cid: " + cid + " of message: " + messageId)
                        .refToMessageId(messageId)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            }
        }
        LOG.debug("Finished handling payloads");

        return partInfoList;
    }

    protected List<PartInfo> getPartInfoList(Ebms3Messaging ebms3Messaging) {
        List<PartInfo> result = new ArrayList<>();

        if (ebms3Messaging.getUserMessage().getPayloadInfo() == null) {
            LOG.trace("UserMessage has no payload info");
            return result;
        }
        final List<Ebms3PartInfo> ebms3PartInfos = ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo();
        if (CollectionUtils.isEmpty(ebms3PartInfos)) {
            return result;
        }

        for (final Ebms3PartInfo ebms3PartInfo : ebms3PartInfos) {
            PartInfo partInfo = convert(ebms3PartInfo);
            partInfo.setPartOrder(result.size());
            result.add(partInfo);
        }

        return result;
    }

    protected PartInfo convert(Ebms3PartInfo ebms3PartInfo) {
        PartInfo result = new PartInfo();

        final Ebms3Description ebms3PartInfoDescription = ebms3PartInfo.getDescription();
        if (ebms3PartInfoDescription != null) {
            Description description = new Description();
            description.setValue(ebms3PartInfoDescription.getValue());
            description.setLang(ebms3PartInfoDescription.getLang());
            result.setDescription(description);
        }
        result.setHref(ebms3PartInfo.getHref());

        final Ebms3PartProperties ebms3PartInfoPartProperties = ebms3PartInfo.getPartProperties();
        if (ebms3PartInfoPartProperties != null) {
            final Set<Ebms3Property> ebms3Properties = ebms3PartInfoPartProperties.getProperty();
            Set<PartProperty> partProperties = new HashSet<>();

            for (Ebms3Property ebms3Property : ebms3Properties) {
                final PartProperty property = partPropertyDictionaryService.findOrCreatePartProperty(ebms3Property.getName(), ebms3Property.getValue(), ebms3Property.getType());
                if (property != null) {
                    partProperties.add(property);
                }
            }

            result.setPartProperties(partProperties);
        }

        return result;
    }


    private boolean isBodyloadCid(String cid) {
        return cid == null || cid.isEmpty() || cid.startsWith(HASH_SIGN);
    }

    protected DataHandler getDataHandler(Node bodyContent) throws TransformerException {
        final Source source = new DOMSource(bodyContent);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Result result = new StreamResult(out);
        final Transformer transformer = xmlUtil.getTransformerFactory().newTransformer();
        transformer.transform(source, result);
        return new DataHandler(new ByteArrayDataSource(out.toByteArray(), "text/xml"));
    }



    protected PartInfo getPartInfoFromFragment(final Ebms3MessageFragmentType messageFragment) {
        if (messageFragment == null) {
            LOG.debug("No message fragment found");
            return null;
        }
        PartInfo partInfo = new PartInfo();
        partInfo.setHref(messageFragment.getHref());
        partInfo.setMime(messageFragment.getMessageHeader().getType().value());

        return partInfo;
    }

    @Transactional
    @Override
    public void persistUpdatedPayloads(List<PartInfo> partInfos) {
        for (PartInfo partInfo : partInfos) {
            partInfoDao.merge(partInfo);
        }
    }
}
