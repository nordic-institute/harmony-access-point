package eu.domibus.core.plugin.transformer;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.plugin.Submission;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.*;

/**
 * @author Ion Perpegel, Catalin Enache
 * @since 4.0.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class SubmissionAS4TransformerTest {

    @Injectable
    private MessageIdGenerator messageIdGenerator;

    @Injectable
    private MpcDictionaryService mpcDictionaryService;

    @Injectable
    private MessagePropertyDictionaryService messagePropertyDictionaryService;

    @Injectable
    private ServiceDictionaryService serviceDictionaryService;

    @Injectable
    private ActionDictionaryService actionDictionaryService;

    @Injectable
    private AgreementDictionaryService agreementDictionaryService;

    @Injectable
    private PartyIdDictionaryService partyIdDictionaryService;

    @Injectable
    private PartyRoleDictionaryService partyRoleDictionaryService;

    @Injectable
    private PartPropertyDictionaryService partPropertyDictionaryService;

    @Injectable
    MshRoleDao mshRoleDao;

    @Tested
    private SubmissionAS4Transformer submissionAS4Transformer;

    @Test
    public void testTransformFromSubmission_generated(final @Mocked Submission submission) {
        String generatedConvId = "guid";

        new Expectations() {{
            messageIdGenerator.generateMessageId();
            result = generatedConvId;

            submission.getConversationId();
            result = null;
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getConversationId();
        Assert.assertEquals(generatedConvId, conversationId);
    }

    @Test
    public void testTransformFromSubmission_empty(final @Mocked Submission submission) {
        String generatedConvId = "guid";

        new Expectations() {{
            messageIdGenerator.generateMessageId();
            result = generatedConvId;

            submission.getConversationId();
            result = StringUtils.EMPTY;
        }};

        String  conversationId = submissionAS4Transformer.transformFromSubmission(submission).getConversationId();
        Assert.assertEquals(StringUtils.EMPTY, conversationId);

    }

    @Test
    public void testTransformFromSubmission_submitted(final @Mocked Submission submission) {
        String submittedConvId = "submittedConvId";
        String generatedConvId = "guid";

        new Expectations() {{
            messageIdGenerator.generateMessageId();
            result = generatedConvId;

            submission.getConversationId();
            result = submittedConvId;
        }};

        String  conversationId = submissionAS4Transformer.transformFromSubmission(submission).getConversationId();
        Assert.assertEquals(submittedConvId, conversationId);
    }

    @Test
    public void testTransformFromMessaging_NotNullUserMessage_TransformationOK(final @Mocked UserMessage userMessage) {

        final String action = "TC1Leg1";
        final String service = "bdx:noprocess";
        final String serviceType = "tc1";
        final String fromRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
        final String toRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


        final PartInfo partInfo = new PartInfo();
        final String fileNameWithoutPath = createFileName();
        partInfo.setFileName(createFileNameWithFullPath(fileNameWithoutPath));

        final Set<PartyId> fromPartyIdSet = createPartyId("domibus-blue");

        final Set<PartyId> toPartyIdSet = createPartyId("domibus-red");

        new Expectations(submissionAS4Transformer) {{

            userMessage.getService().getValue();
            result = service;

            userMessage.getService().getType();
            result = serviceType;

            userMessage.getActionValue();
            result = action;

            userMessage.getPartyInfo().getFrom().getFromPartyId();
            result = fromPartyIdSet;

            userMessage.getPartyInfo().getTo().getToPartyId();
            result = toPartyIdSet;

        }};

        final Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage, Collections.singletonList(partInfo));
        Assert.assertNotNull(submission);

        new Verifications() {{
            Submission submissionActual;
            PartInfo partInfoActual;
            submissionAS4Transformer.addPayload(submissionActual = withCapture(), partInfoActual = withCapture());
            Assert.assertNotNull(submissionActual);
            Assert.assertNotNull(partInfoActual);
            times = 1;
        }};
    }


    @Test
    public void testAddPayload() {
        final String fileNameWithoutPath = createFileName();
        final String fileNameFull = createFileNameWithFullPath(fileNameWithoutPath);

        final Submission submission = new Submission();
        final PartInfo partInfo = new PartInfo();

        HashSet<PartProperty> partProperties1 = new HashSet<>();
        PartProperty property = new PartProperty();
        property.setValue("MimeType");
        property.setName("text/xml");
        property.setType("string");
        partProperties1.add(property);
        partInfo.setPartProperties(partProperties1);
        partInfo.setFileName(fileNameFull);

        //tested method
        submissionAS4Transformer.addPayload(submission, partInfo);

        Assert.assertNotNull(submission);
        Assert.assertEquals(1, submission.getPayloads().size());
        Submission.Payload payload = submission.getPayloads().iterator().next();
        Assert.assertEquals(2, payload.getPayloadProperties().size());
        Iterator<Submission.TypedProperty> typedProperties = payload.getPayloadProperties().iterator();
        Submission.TypedProperty typedProperty = typedProperties.next();
        Submission.TypedProperty typedProperty2 = typedProperties.next();

        Assert.assertEquals("text/xml", typedProperty.getKey());
        Assert.assertEquals("MimeType", typedProperty.getValue());

        Assert.assertEquals("FileName", typedProperty2.getKey());
        Assert.assertEquals(fileNameWithoutPath, typedProperty2.getValue());

    }


    @Test
    public void testTransformFromMessaging_NullUserMessage_TransformationNOK() {

        final UserMessage userMessage = null;

        final Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage, null);
        Assert.assertNotNull(submission);
    }

    private String createFileName() {
        return UUID.randomUUID() + ".payload";
    }

    private String createFileNameWithFullPath(String fileNameWithoutPath) {
        return File.separator + "domibus" + File.separator + "payloads" + File.separator + fileNameWithoutPath;
    }

    private Set<PartyId> createPartyId(String partyIdValue) {
        final PartyId partyId = new PartyId();
        partyId.setValue(partyIdValue);
        partyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        return Collections.singleton(partyId);
    }
}
