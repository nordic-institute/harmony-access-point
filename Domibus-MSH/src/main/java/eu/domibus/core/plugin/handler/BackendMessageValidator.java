package eu.domibus.core.plugin.handler;

import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Role;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
import static eu.domibus.api.util.DomibusStringUtil.*;
import static eu.domibus.logging.DomibusMessageCode.*;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Arun Raj
 * @author Federico Martini
 * @since 3.3
 * <br>
 * This class validates the content of the UserMessage which represents the message's header.
 * These validations are based on the AS4 specifications and the gateway PMode configuration.
 * <p>
 * Since any RuntimeException rollbacks the transaction and we don't want that now (because the client would receive a JTA Transaction error as response),
 * the class uses the "noRollbackFor" attribute inside the @Transactional annotation.
 * <p>
 * TODO EbMS3Exception will be soon replaced with a custom Domibus exception in order to report this validation errors.
 */

@Service
public class BackendMessageValidator {

    public static final String MESSAGE_WITH_ID_STR = "Message with id [";

    public static final String PARTY_INFO_TO_PARTY_ID = "PartyInfo/To/PartyId";
    public static final String PARTY_INFO_FROM_PARTY_ID = "PartyInfo/From/PartyId";
    protected static final String KEY_MESSAGEID_PATTERN = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendMessageValidator.class);
    public static final String ALREADY_EXISTS_MESSAGE_IDENTIFIERS_MUST_BE_UNIQUE = "] already exists. Message identifiers must be unique";
    public static final String SERVICE = "Service";
    public static final String SERVICE_TYPE = "ServiceType";
    public static final String ACTION = "Action";
    public static final String PART_PROPERTY = "PartProperty";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private MessagePropertyValidator messagePropertyValidator;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    /**
     * Validations pertaining to the field - UserMessage/MessageInfo/MessageId<br><br>
     * <b><u>As per ebms_core-3.0-spec-cs-02.pdf:</u></b><br>
     * &ldquo;b:Messaging/eb:UserMessage/eb:MessageInfo/eb:MessageId:
     * This REQUIRED element has a value representing – for each message - a globally unique identifier <b>conforming to MessageId [RFC2822].</b>
     * Note: In the Message-Id and Content-Id MIME headers, values are always surrounded by angle brackets. However references in mid: or cid: scheme URI's and
     * the MessageId and RefToMessageId elements MUST NOT include these delimiters.&rdquo;<br><br>
     * <p>
     * <b><u>As per RFC2822 :</u></b><br>
     * &ldquo;2.1. General Description - At the most basic level, a message is a series of characters.  A message that is conformant with this standard is comprised of
     * characters with values in the range 1 through 127 and interpreted as US-ASCII characters [ASCII].&rdquo;<br><br>
     * <p>
     * &ldquo;3.6.4. Identification fields: The "Message-ID:" field provides a unique message identifier that refers to a particular version of a particular message.
     * The uniqueness of the message identifier is guaranteed by the host that generates it (see below).
     * This message identifier is <u>intended to be machine readable and not necessarily meaningful to humans.</u>
     * A message identifier pertains to exactly one instantiation of a particular message; subsequent revisions to the message each receive new message identifiers.&rdquo;<br><br>
     * <p>
     * Though the above specifications state the message id can be any ASCII character, practically the message ids might need to be referenced by persons and documents.
     * Hence all non printable characters (ASCII 0 to 31 and 127) should be avoided.<br><br>
     * <p>
     * RFC2822 also states the better algo for generating a unique id is - put a combination of the current absolute date and time along with
     * some other currently unique (perhaps sequential) identifier available on the system + &ldquo;@&rdquo; + domain name (or a domain literal IP address) of the host on which the
     * message identifier. As seen from acceptance and production setup, existing clients of Domibus sending message id is not following this format. Hence, although it is good, it is not enforced.
     * Only control character restriction is enforced.
     *
     * @param messageId the message id.
     * @throws EbMS3Exception if the message id value is invalid
     */
    public void validateMessageId(final String messageId) throws EbMS3Exception, DuplicateMessageException {
        if (isBlank(messageId)) {
            LOG.debug("Message id is empty: validation skipped");
            return;
        }

        if (isTrimmedStringLengthLongerThanDefaultMaxLength(messageId)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "MessageId", messageId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("Value of MessageId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .refToMessageId(messageId)
                    .build();
        }

        validateMessageIdPattern(messageId, "eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:MessageId");

        // handle if the messageId is unique. This should only fail if the ID is set from the outside
        if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
            LOG.businessError(DUPLICATE_MESSAGEID, messageId);
            throw new DuplicateMessageException(MESSAGE_WITH_ID_STR + messageId + "] already exists. Message identifiers must be unique.");
        }
    }


    /**
     * The field - UserMessage/MessageInfo/RefToMessageId is expected to satisfy all the validations of the - UserMessage/MessageInfo/MessageId field
     *
     * @param refToMessageId the message id to be validated.
     * @throws EbMS3Exception if the RefToMessageId value is invalid
     */
    public void validateRefToMessageId(final String refToMessageId) throws EbMS3Exception {
        //refToMessageId is an optional element and can be null
        if (refToMessageId == null) {
            return;
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(refToMessageId)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "RefToMessageId", refToMessageId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("RefToMessageId value" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
        validateMessageIdPattern(refToMessageId, "eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:RefToMessageId");
    }

    /* Validating for presence of non printable control characters.
     * This validation will be skipped if the pattern is not present in the configuration file.
     */
    protected void validateMessageIdPattern(String messageId, String elementType) throws EbMS3Exception {
        String messageIdPattern = domibusPropertyProvider.getProperty(KEY_MESSAGEID_PATTERN);
        LOG.debug("MessageIdPattern read from file is [{}]", messageIdPattern);

        if (isBlank(messageIdPattern)) {
            return;
        }
        Pattern patternNoControlChar = Pattern.compile(messageIdPattern);
        Matcher m = patternNoControlChar.matcher(messageId);
        if (!m.matches()) {
            LOG.businessError(VALUE_DO_NOT_CONFORM_TO_MESSAGEID_PATTERN, elementType, messageIdPattern, messageId);
            String errorMessage = "Element " + elementType + " does not conform to the required MessageIdPattern: " + messageIdPattern;
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message(errorMessage)
                    .refToMessageId(messageId)
                    .build();
        }
    }

    /**
     * Verifies that the initiator and the responder parties are different.
     *
     * @param from the initiator party.
     * @param to   the responder party.
     * @throws NullPointerException if either initiator party or responder party is null
     */
    public void validateParties(Party from, Party to) {

        Validate.notNull(from, "Initiator party was not found");
        Validate.notNull(to, "Responder party was not found");
    }

    /**
     * Verifies that the message is being sent by the same party as the one configured for the sending access point
     *
     * @param gatewayParty the access point party.
     * @param from         the initiator party.
     * @throws NullPointerException if either gatewayParty or initiator party is null
     * @throws EbMS3Exception       if the initiator party name does not correspond to the access point's name
     */
    public void validateInitiatorParty(Party gatewayParty, Party from) throws EbMS3Exception {

        Validate.notNull(gatewayParty, "Access point party was not found");
        Validate.notNull(from, "Initiator party was not found");

        if (!gatewayParty.equals(from)) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("The initiator party's name [" + from.getName() + "] does not correspond to the access point's name [" + gatewayParty.getName() + "]")
                    .mshRole(MSHRole.SENDING)
                    .build();
        }
    }

    /**
     * Verifies that the message is not for the current gateway.
     *
     * @param gatewayParty the access point party.
     * @param to           the responder party.
     * @throws NullPointerException if either access point party or responder party is null
     */
    public void validateResponderParty(Party gatewayParty, Party to) {

        Validate.notNull(gatewayParty, "Access point party was not found");
        Validate.notNull(to, "Responder party was not found");
    }

    /**
     * Verifies that the parties' roles are different
     *
     * @param fromRole the role of the initiator party.
     * @param toRole   the role of the responder party.
     * @throws NullPointerException if either initiator party's role or responder party's role is null
     * @throws EbMS3Exception       if the initiator party's role is the same as the responder party's role
     */
    public void validatePartiesRoles(Role fromRole, Role toRole) throws EbMS3Exception {

        Validate.notNull(fromRole, "Role of the initiator party was not found");
        Validate.notNull(toRole, "Role of the responder party was not found");

        if (fromRole.equals(toRole)) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("The initiator party's role is the same as the responder party's one[" + fromRole.getName() + "]")
                    .build();
        }
    }

    public void validatePayloads(List<PartInfo> partInfos) {
        if (partInfos == null || isEmpty(partInfos)) {
            return;
        }

        for (PartInfo partInfo : partInfos) {
            validateCompressionProperty(partInfo.getPartProperties());
        }
    }

    protected void validateCompressionProperty(Set<PartProperty> properties) throws SubmissionValidationException {
        if (properties == null || isEmpty(properties)) {
            return;
        }

        for (Property property : properties) {
            if (CompressionService.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName())) {
                throw new SubmissionValidationException("The occurrence of the property " + CompressionService.COMPRESSION_PROPERTY_KEY + " and its value are fully controlled by the AS4 compression feature");
            }
        }
    }

    /**
     * Validates the essential fields in a {@link UserMessage} to ensure elements necessary for pMode matching have been provided by the user.
     */
    protected void validateUserMessageForPmodeMatch(Submission submission, MSHRole mshRole) throws EbMS3Exception, DuplicateMessageException {
        if (submission == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory header metadata UserMessage is not provided.")
                    .build();
        }
        try {
            validateMessageInfo(submission);  // MessageInfo is always initialized in the get method
            validatePartyInfoForPModeMatch(submission);
            validateCollaborationInfo(submission);
        } catch (EbMS3Exception ebms3ex) {
            ebms3ex.setMshRole(mshRole);
            throw ebms3ex;
        }
    }

    protected void validateMessageInfo(Submission submission) throws EbMS3Exception, DuplicateMessageException {
        if (submission == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "MessageInfo");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory header metadata UserMessage/MessageInfo is not provided.")
                    .build();
        }
        validateMessageId(submission.getMessageId());
        //check duplicate message id
        validateRefToMessageId(submission.getRefToMessageId());
    }

    protected void validatePartyInfoForPModeMatch(Submission submission) throws EbMS3Exception {
        validateFromPartyId(submission);
        validateFromRole(submission.getFromRole());
        validateToPartyIdForPModeMatch(submission);
        validateToRoleForPModeMatch(submission);
    }

    protected void validateFromPartyId(Submission submission) throws EbMS3Exception {
        if (CollectionUtils.isEmpty(submission.getFromParties())) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field From PartyId is not provided.")
                    .build();
        }
        final Submission.Party party = submission.getFromParties().iterator().next();

        if (isBlank(party.getPartyId())) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_FROM_PARTY_ID);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field From PartyId is not provided.")
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(party.getPartyId())) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, PARTY_INFO_FROM_PARTY_ID, party.getPartyId());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("From PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(party.getPartyIdType())) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "From PartyIdType", party.getPartyIdType());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("From PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }

    }

    protected void validateFromRole(String fromRole) throws EbMS3Exception {
        if (isBlank(fromRole)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From/Role");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field From Role is not provided.")
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(fromRole)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "PartyInfo/From/Role", fromRole);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("From Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
    }

    protected void validateToPartyIdForPModeMatch(Submission submission) throws EbMS3Exception {
        if (CollectionUtils.isEmpty(submission.getToParties())) {
            //In scenario of DynamicDiscovery Backend will not provide To/PartyId details, it is discovered by Domibus during PMode match.
            //Hence elements To and To/Party are optional
            return;
        }
        final Submission.Party toParty = submission.getToParties().iterator().next();

        if (isBlank(toParty.getPartyId())) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_TO_PARTY_ID);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field To PartyId is not provided.")
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(toParty.getPartyId())) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, PARTY_INFO_TO_PARTY_ID, toParty.getPartyId());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("To PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(toParty.getPartyIdType())) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "To PartyIdType", toParty.getPartyIdType());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("To PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }

    }

    protected void validateToRoleForPModeMatch(Submission submission) throws EbMS3Exception {
        if (CollectionUtils.isEmpty(submission.getToParties())) {
            //In scenario of DynamicDiscovery Backend will not provide To/PartyId details, it is discovered by Domibus during PMode match.
            //Hence elements To and To/Role are optional
            return;
        }
        String toRole = submission.getToRole();
        if (isBlank(toRole)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/To/Role");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field To Role is not provided.")
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(toRole)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "PartyInfo/To/Role", toRole);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("To Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
    }

    protected void validateCollaborationInfo(Submission submission) throws EbMS3Exception {
        if (submission == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage/CollaborationInfo");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field UserMessage/CollaborationInfo is not provided.")
                    .build();
        }
        validateAgreementRef(submission.getAgreementRef(), submission.getAgreementRefType());
        validateService(submission.getService(), submission.getServiceType());
        validateAction(submission.getAction());
        validateConversationId(submission.getConversationId());
    }

    /**
     * The field - UserMessage/CollaborationInfo/AgreementRef is expected to satisfy all the validations of the - Messaging/UserMessage/CollaborationInfo/AgreementRef  field defined in http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/os/ebms_core-3.0-spec-os.pdf
     *
     * @param value the value of the AgreementRef to be validated.
     * @param type  the type of the AgreementRef to be validated.
     * @throws EbMS3Exception if the AgreementRef value is invalid
     */
    protected void validateAgreementRef(String value, String type) throws EbMS3Exception {
        //agreementRef is an optional element and can be null
        if (StringUtils.isBlank(value)) {
            LOG.debug("Optional field AgreementRef is null");
            return;
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(value)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "AgreementRef", value);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("AgreementRef Value" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(type)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "AgreementRef Type", type);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("AgreementRef Type" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
    }

    protected void validateService(String serviceValue, String serviceType) throws EbMS3Exception {
        if (isBlank(serviceValue)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, SERVICE);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field Service is not provided.")
                    .build();
        }

        if (isTrimmedStringLengthLongerThanDefaultMaxLength(serviceValue)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, SERVICE, serviceValue);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(SERVICE + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(serviceType)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, SERVICE_TYPE, serviceType);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(SERVICE_TYPE + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
    }

    protected void validateAction(String action) throws EbMS3Exception {
        if (isBlank(action)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, ACTION);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0009)
                    .message("Mandatory field Action is not provided.")
                    .build();
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(action)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, ACTION, action);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(ACTION + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH)
                    .build();
        }
    }

    /**
     * The field - UserMessage/CollaborationInfo/ConversationId is expected to satisfy all the validations of the - eb:UserMessage/eb:CollaborationInfo/eb:ConversationId field defined in eDelivery AS4 profile
     */
    protected void validateConversationId(String conversationId) throws EbMS3Exception {
        //conversationId is an optional element
        if (isBlank(conversationId)) {
            LOG.debug("Optional field ConversationId is null or empty");
            return;
        }
        if (isTrimmedStringLengthLongerThanDefaultMaxLength(conversationId)) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("ConversationId is too long (over 255 characters)")
                    .build();
        }
    }

    public void validateSubmissionSending(Submission submission) throws DuplicateMessageException, EbMS3Exception {
        validateUserMessageForPmodeMatch(submission, MSHRole.SENDING);
        messagePropertyValidator.validate(submission, MSHRole.SENDING);
        validateSubmissionPayload(submission, MSHRole.SENDING);
    }

    public void validatePayloadProfile(UserMessage userMessage, List<PartInfo> partInfos, String pModeKey) throws EbMS3Exception {
        payloadProfileValidator.validate(userMessage, partInfos, pModeKey);
    }

    public void validatePropertyProfile(UserMessage userMessage, String pModeKey) throws EbMS3Exception {
        propertyProfileValidator.validate(userMessage, pModeKey);
    }

    public void validateMessageIsUnique(String messageId) throws DuplicateMessageException {
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        if (!MessageStatus.NOT_FOUND.equals(messageStatus)) {
            throw new DuplicateMessageException(MESSAGE_WITH_ID_STR + messageId + ALREADY_EXISTS_MESSAGE_IDENTIFIERS_MUST_BE_UNIQUE);
        }
    }

    public void validateSubmissionPayload(Submission submission, MSHRole mshRole) throws EbMS3Exception {
        if(submission.getPayloads() == null){
            return;
        }
        if(submission.getPayloads().size() > 28){
            LOG.businessError(BUS_ATTACHMENTS_MORE_THAN_28);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(BUS_ATTACHMENTS_MORE_THAN_28.getMessage())
                    .mshRole(mshRole)
                    .build();
        }
        for (Submission.Payload submissionPayload : submission.getPayloads()) {
            validateSubmissionPartInfoProperties(submissionPayload.getPayloadProperties(), mshRole);
        }
    }

    protected void validateSubmissionPartInfoProperties(Collection<Submission.TypedProperty> payloadProperties, MSHRole mshRole) throws EbMS3Exception {
        for (Submission.TypedProperty payloadProperty : payloadProperties) {
            validateSubmissionPayloadProperty(payloadProperty, mshRole);
        }
    }

    protected void validateSubmissionPayloadProperty(Submission.TypedProperty payloadProperty, MSHRole mshRole) throws EbMS3Exception {
        if(payloadProperty == null || payloadProperty.getValue() == null){
            LOG.debug("Payload properties empty in usermessage submission");
            return;
        }
        String payloadPropertyValue = payloadProperty.getValue();
        if (DomibusStringUtil.isStringLengthLongerThan1024Chars(payloadPropertyValue)) {
            LOG.businessError(VALUE_LONGER_THAN_STRING_LENGTH_1024, PART_PROPERTY, payloadPropertyValue);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(PART_PROPERTY + ERROR_MSG_STRING_LONGER_THAN_STRING_LENGTH_1024)
                    .mshRole(mshRole)
                    .build();
        }
    }
}
