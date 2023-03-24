package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.dictionary.PartPropertyDictionaryService;
import eu.domibus.core.util.SoapUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.*;

import static org.junit.Assert.fail;

@RunWith(JMockit.class)
public class UserMessagePayloadServiceTest {

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    protected SoapUtil soapUtil;

    @Injectable
    protected XMLUtil xmlUtil;

    @Injectable
    protected PartInfoDao partInfoDao;

    @Injectable
    protected PartPropertyDictionaryService partPropertyDictionaryService;


    @Tested
    UserMessagePayloadServiceImpl userMessagePayloadService;

    @Test
    public void test_HandlePayLoads_HappyFlowUsingEmptyCID(@Injectable final Ebms3Messaging ebms3Messaging,
                                                           @Injectable final PartInfo partInfo) throws SOAPException, TransformerException, EbMS3Exception {

        Ebms3PartInfo ebms3PartInfo = new Ebms3PartInfo();
        ebms3PartInfo.setHref(null);
        Ebms3Description value1 = new Ebms3Description();
        value1.setValue("description");
        value1.setLang("en");
        ebms3PartInfo.setDescription(value1);
        Ebms3Schema value = new Ebms3Schema();
        value.setLocation("location");
        value.setNamespace("namespace");
        value.setVersion("version");
        ebms3PartInfo.setSchema(value);

        Ebms3PayloadInfo ebms3PayloadInfo = new Ebms3PayloadInfo();
        ebms3PayloadInfo.getPartInfo().add(ebms3PartInfo);
        new Expectations(userMessagePayloadService) {{
            ebms3Messaging.getUserMessage().getPayloadInfo();
            result = ebms3PayloadInfo;

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            userMessagePayloadService.convert(ebms3PartInfo);
            result = partInfo;
        }};

        userMessagePayloadService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new Verifications() {{
            partInfo.setInBody(true);
            partInfo.setPayloadDatahandler((DataHandler) any);
        }};
    }

    @Test
    public void test_HandlePayLoads_EmptyCIDAndBodyContent(@Injectable final Ebms3Messaging ebms3Messaging,
                                                           @Injectable final Node bodyContent,
                                                           @Injectable final PartInfo partInfo)
            throws SOAPException, TransformerException, EbMS3Exception {

        new Expectations() {{

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo();
            result = null;
        }};

        userMessagePayloadService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new FullVerifications() {{
        }};
    }


    /**
     * A single message having multiple PartInfo's with no or special cid.
     */
    @Test
    public void test_HandlePayLoads_NullCIDMultiplePartInfo(
            @Injectable final Ebms3Messaging ebms3Messaging,
            @Injectable final Node bodyContent1,
            @Injectable final DataHandler dataHandler)
            throws SOAPException, TransformerException {

        Ebms3PartInfo part1 = UserMessageHandlerServiceImplTest.getPartInfo("MimeType", "text/xml", "");
        Ebms3PartInfo part2 = UserMessageHandlerServiceImplTest.getPartInfo("MimeType", "text/xml", "#1234");
        List<Ebms3PartInfo> ebms3PartInfos = Arrays.asList(
                part1,
                part2);

        List<Node> bodyContentNodeList = new ArrayList<>();
        bodyContentNodeList.add(bodyContent1);
        final Iterator<Node> bodyContentNodeIterator = bodyContentNodeList.iterator();

        new Expectations(userMessagePayloadService) {{
            userMessagePayloadService.getDataHandler((Node) any);
            result = dataHandler;

            soapRequestMessage.getAttachments();
            result = Collections.emptyIterator();

            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo();
            result = ebms3PartInfos;

            partPropertyDictionaryService.findOrCreatePartProperty(anyString, anyString, anyString);
            result = new PartProperty();
            times = 2;
        }};

        try {
            userMessagePayloadService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            fail("Expecting error that - More than one Partinfo referencing the soap body found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }

        new Verifications() {
        };
    }

    @Test
    public void test_HandlePayLoads_HappyFlowUsingCID(@Injectable final UserMessage userMessage,
                                                      @Injectable final Ebms3Messaging ebms3Messaging,
                                                      @Injectable final AttachmentPart attachmentPart1,
                                                      @Injectable final AttachmentPart attachmentPart2,
                                                      @Injectable final DataHandler attachmentPart1DH,
                                                      @Injectable final DataHandler attachmentPart2DH) throws SOAPException, TransformerException, EbMS3Exception {

        final Ebms3PartInfo partInfo = new Ebms3PartInfo();
        partInfo.setHref("cid:message");

        Ebms3PartProperties partProperties = new Ebms3PartProperties();
        Ebms3Property property1 = new Ebms3Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");

        partProperties.getProperties().add(property1);
        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{
            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo();
            result = Arrays.asList(partInfo);

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message";

            attachmentPart2.getDataHandler();
            result = attachmentPart2DH;
        }};

        userMessagePayloadService.handlePayloads(soapRequestMessage, ebms3Messaging, null);

        new Verifications() {{
        }};
    }

    @Test
    public void test_HandlePayLoads_NoPayloadFound(
            @Injectable final UserMessage userMessage,
            @Injectable final Ebms3Messaging ebms3Messaging,
            @Injectable final PartInfo partInfo,
            @Injectable final AttachmentPart attachmentPart1,
            @Injectable final AttachmentPart attachmentPart2) throws TransformerException, SOAPException {

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations(userMessagePayloadService) {{
            ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "messageId";

            userMessagePayloadService.getPartInfoList(ebms3Messaging);
            result = Arrays.asList(partInfo);

            partInfo.getHref();
            result = "cid:message";

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message123";

        }};

        try {
            userMessagePayloadService.handlePayloads(soapRequestMessage, ebms3Messaging, null);
            fail("Expected Ebms3 exception that no matching payload was found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0011, e.getErrorCode());
        }

        new FullVerifications() {{
            attachmentPart1.setContentId(anyString);
            attachmentPart2.setContentId(anyString);
        }};
    }
}
