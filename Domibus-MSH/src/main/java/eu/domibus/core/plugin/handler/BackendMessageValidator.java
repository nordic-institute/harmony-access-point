package eu.domibus.core.plugin.handler;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Role;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public static final String PARTY_INFO_TO_PARTY_ID = "PartyInfo/To/PartyId";
    public static final String PARTY_INFO_FROM_PARTY_ID = "PartyInfo/From/PartyId";
    protected static final String KEY_MESSAGEID_PATTERN = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendMessageValidator.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DomainContextProvider domainContextProvider;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

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
            EbMS3Exception exc = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name [" + from.getName() + "] does not correspond to the access point's name [" + gatewayParty.getName() + "]", null, null);
            exc.setMshRole(MSHRole.SENDING);
            throw exc;
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
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's role is the same as the responder party's one[" + fromRole.getName() + "]", null, null);
        }
    }

    public void validatePayloads(PayloadInfo payloadInfo) throws EbMS3Exception {
        if (payloadInfo == null || isEmpty(payloadInfo.getPartInfo())) {
            return;
        }

        for (PartInfo partInfo : payloadInfo.getPartInfo()) {
            validateCompressionProperty(partInfo.getPartProperties());
        }
    }

    protected void validateCompressionProperty(PartProperties properties) throws SubmissionValidationException {
        if (properties == null || isEmpty(properties.getProperties())) {
            return;
        }

        for (Property property : properties.getProperties()) {
            if (CompressionService.COMPRESSION_PROPERTY_KEY.equalsIgnoreCase(property.getName())) {
                throw new SubmissionValidationException("The occurrence of the property " + CompressionService.COMPRESSION_PROPERTY_KEY + " and its value are fully controlled by the AS4 compression feature");
            }
        }
    }

    /**
     * Validates the essential fields in a {@link UserMessage} to ensure elements necessary for pMode matching have been provided by the user.
     *
     * @param userMessage
     * @param mshRole
     * @throws EbMS3Exception
     * @throws DuplicateMessageException
     */
    public void validateUserMessageForPmodeMatch(UserMessage userMessage, MSHRole mshRole) throws EbMS3Exception, DuplicateMessageException {
        if (userMessage == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory header metadata UserMessage is not provided.", null, null);
        }
        try {
            validateMessageInfo(userMessage.getMessageInfo());  // MessageInfo is always initialized in the get method
            validatePartyInfo(userMessage.getPartyInfo());
            validateCollaborationInfo(userMessage.getCollaborationInfo());
        } catch (EbMS3Exception ebms3ex) {
            ebms3ex.setMshRole(mshRole);
            throw ebms3ex;
        }
    }

    protected void validateMessageInfo(MessageInfo messageInfo) throws EbMS3Exception, DuplicateMessageException {
        if (messageInfo == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "MessageInfo");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory header metadata UserMessage/MessageInfo is not provided.", null, null);
        }
        validateMessageId(messageInfo.getMessageId());
        //check duplicate message id
        validateRefToMessageId(messageInfo.getRefToMessageId());
    }

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
        if (messageId == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "MessageId");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field MessageId is not provided.", null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(messageId, DO_NOT_TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "MessageId", messageId);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Value of MessageId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, messageId, null);
        }
        validateMessageIdPattern(messageId, "eb:Messaging/eb:UserMessage/eb:MessageInfo/eb:MessageId");

        // handle if the messageId is unique. This should only fail if the ID is set from the outside
        if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
            LOG.businessError(DUPLICATE_MESSAGEID, messageId);
            throw new DuplicateMessageException("Message with id [" + messageId + "] already exists. Message identifiers must be unique.");
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
        if (isStringLengthGreaterThanDefaultMaxLength(refToMessageId, DO_NOT_TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "RefToMessageId", refToMessageId);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "RefToMessageId value" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
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
            String errorMessage = "Value of " + elementType + " does not conform to the required MessageIdPattern: " + messageIdPattern;
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, errorMessage, messageId, null);
        }
    }

    protected void validatePartyInfo(PartyInfo partyInfo) throws EbMS3Exception {
        if (partyInfo == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field PartyInfo is not provided.", null, null);
        }
        validateFromPartyId(partyInfo.getFrom());
        validateFromRole(partyInfo.getFrom());
        validateToPartyId(partyInfo.getTo());
        validateToRole(partyInfo.getTo());
    }

    protected void validateFromPartyId(From from) throws EbMS3Exception {
        if (from == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field PartyInfo/From is not provided.", null, null);
        }
        final Set<PartyId> fromParties = from.getPartyId();
        if (isEmpty(fromParties)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_FROM_PARTY_ID);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field From PartyId is not provided.", null, null);
        }
        for (PartyId fromParty : fromParties) {
            if (isBlank(fromParty.getValue())) {
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_FROM_PARTY_ID);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field From PartyId is not provided.", null, null);
            }
            if (isStringLengthGreaterThanDefaultMaxLength(fromParty.getValue(), TRIM)) {
                LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, PARTY_INFO_FROM_PARTY_ID, fromParty.getValue());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "From PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
            }
            if (isBlank(fromParty.getType())) {
                //Although for AS4 the PartyIdType is optional, the ICD for default plugins states the PartyIdType is mandatory
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "From PartyIdType");
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field From PartyIdType is not provided.", null, null);
            }
            if (isStringLengthGreaterThanDefaultMaxLength(fromParty.getType(), TRIM)) {
                LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "From PartyIdType", fromParty.getType());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "From PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
            }
        }
    }

    protected void validateFromRole(From from) throws EbMS3Exception {
        if (from == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field PartyInfo/From is not provided.", null, null);
        }
        final String fromRole = from.getRole();
        if (isBlank(fromRole)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/From/Role");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field From Role is not provided.", null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(fromRole, TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "PartyInfo/From/Role", fromRole);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "From Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
    }

    protected void validateToPartyId(To to) throws EbMS3Exception {
        if (to == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/To");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field PartyInfo/To is not provided.", null, null);
        }
        final Set<PartyId> toParties = to.getPartyId();
        if (isEmpty(toParties)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_TO_PARTY_ID);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field To PartyId is not provided.", null, null);
        }
        for (PartyId toParty : toParties) {
            if (isBlank(toParty.getValue())) {
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, PARTY_INFO_TO_PARTY_ID);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field To PartyId is not provided.", null, null);
            }
            if (isStringLengthGreaterThanDefaultMaxLength(toParty.getValue(), TRIM)) {
                LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, PARTY_INFO_TO_PARTY_ID, toParty.getValue());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "To PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
            }
            if (isBlank(toParty.getType())) {
                //Although for AS4 the PartyIdType is optional, the ICD for default plugins states the PartyIdType is mandatory
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "To PartyIdType");
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field To PartyIdType is not provided.", null, null);
            }
            if (isStringLengthGreaterThanDefaultMaxLength(toParty.getType(), TRIM)) {
                LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "To PartyIdType", toParty.getType());
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "To PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
            }
        }
    }

    protected void validateToRole(To to) throws EbMS3Exception {
        if (to == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/To");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field PartyInfo/To is not provided.", null, null);
        }
        final String toRole = to.getRole();
        if (isBlank(toRole)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "PartyInfo/To/Role");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field To Role is not provided.", null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(toRole, TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "PartyInfo/To/Role", toRole);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "To Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
    }

    protected void validateCollaborationInfo(CollaborationInfo collaborationInfo) throws EbMS3Exception {
        if (collaborationInfo == null) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage/CollaborationInfo");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field UserMessage/CollaborationInfo is not provided.", null, null);
        }
        validateAgreementRef(collaborationInfo.getAgreementRef());
        validateService(collaborationInfo.getService());
        validateAction(collaborationInfo.getAction());
        validateConversationId(collaborationInfo.getConversationId());
    }

    /**
     * The field - UserMessage/CollaborationInfo/AgreementRef is expected to satisfy all the validations of the - Messaging/UserMessage/CollaborationInfo/AgreementRef  field defined in http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/core/os/ebms_core-3.0-spec-os.pdf
     *
     * @param agreementRef the AgreementRef to be validated.
     * @throws EbMS3Exception if the AgreementRef value is invalid
     */
    protected void validateAgreementRef(AgreementRef agreementRef) throws EbMS3Exception {
        //agreementRef is an optional element and can be null
        if (agreementRef == null) {
            LOG.debug("Optional field AgreementRef is null");
            return;
        }
        if (isStringLengthGreaterThanDefaultMaxLength(agreementRef.getValue(), TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "AgreementRef", agreementRef.getValue());
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "AgreementRef Value" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(agreementRef.getType(), TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "AgreementRef Type", agreementRef.getType());
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "AgreementRef Type" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(agreementRef.getPmode(), TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "AgreementRef Pmode", agreementRef.getPmode());
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "AgreementRef Pmode is too long (over 255 characters)", agreementRef.getPmode(), null);
        }
    }

    protected void validateService(final eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception {
        if (service == null || isBlank(service.getValue())) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "Service");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field Service is not provided.", null, null);
        }
        final String serviceValue = service.getValue();
        final String serviceType = service.getType();
        if (isStringLengthGreaterThanDefaultMaxLength(serviceValue, TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "Service", serviceValue);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Service" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(serviceType, TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "ServiceType", serviceType);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "ServiceType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
    }

    protected void validateAction(String action) throws EbMS3Exception {
        if (isBlank(action)) {
            LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "Action");
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0009, "Mandatory field Action is not provided.", null, null);
        }
        if (isStringLengthGreaterThanDefaultMaxLength(action, TRIM)) {
            LOG.businessError(VALUE_LONGER_THAN_DEFAULT_STRING_LENGTH, "Action", action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Action" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH, null, null);
        }
    }

    /**
     * The field - UserMessage/CollaborationInfo/ConversationId is expected to satisfy all the validations of the - eb:UserMessage/eb:CollaborationInfo/eb:ConversationId field defined in eDelivery AS4 profile
     *
     * @param conversationId
     * @throws EbMS3Exception
     */
    protected void validateConversationId(String conversationId) throws EbMS3Exception {
        //conversationId is an optional element
        if (isBlank(conversationId)) {
            LOG.debug("Optional field ConversationId is null or empty");
            return;
        }
        if (isStringLengthGreaterThanDefaultMaxLength(conversationId, TRIM)) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "ConversationId is too long (over 255 characters)", conversationId, null);
        }
    }
}
