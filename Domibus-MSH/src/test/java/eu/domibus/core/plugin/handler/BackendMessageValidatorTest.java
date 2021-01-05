package eu.domibus.core.plugin.handler;

import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Role;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.messaging.DuplicateMessageException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.UUID;

import static eu.domibus.api.util.DomibusStringUtil.ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH;
import static eu.domibus.core.plugin.handler.DatabaseMessageHandlerTest.createUserMessage;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class BackendMessageValidatorTest {
    private static final String MESSAGE_ID_PATTERN = "^[\\x20-\\x7E]*$";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String INITIATOR_ROLE_NAME = "defaultInitiatorRole";
    private static final String RESPONDER_ROLE_NAME = "defaultResponderRole";
    private static final String INITIATOR_ROLE_VALUE = "defaultInitiator";
    private static final String RESPONDER_ROLE_VALUE = "defaultResponder";
    private static final String MESS_ID = UUID.randomUUID().toString();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Tested
    private BackendMessageValidator backendMessageValidatorObj;

    @Test
    public void validateMessageId() throws Exception {

        new Expectations() {{
            domibusPropertyProvider.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = MESSAGE_ID_PATTERN;

            userMessageLogDao.getMessageStatus(anyString);
            result = MessageStatus.NOT_FOUND;
        }};

        /*Happy Flow No error should occur*/
        try {
            String messageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateMessageId(messageId1);

            String messageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId1_1);

            String messageId1_2 = "APP-RESPONSE-d8d85972-64fb-4161-a1fb-996aa7a9c39c-DOCUMENT-BUNDLE";
            backendMessageValidatorObj.validateMessageId(messageId1_2);

            String messageId1_3 = "<1234>";
            backendMessageValidatorObj.validateMessageId(messageId1_3);

            String messageId1_4 = "^12^3$4";
            backendMessageValidatorObj.validateMessageId(messageId1_4);

        } catch (EbMS3Exception e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }
        /*Happy Flow No error should occur*/

        /*Message Id with leading and/or trailing whitespaces should throw error*/
        try {
            String messageId2 = "\t\t346ea37f-7583-40b0-9ffc-3f4cfa88bf8b@domibus.eu\t\t";
            backendMessageValidatorObj.validateMessageId(messageId2);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id with leading and/or trailing whitespaces should throw error*/


        /*Message Id containing non printable control characters should result in error*/
        try {
            String messageId4 = "346ea\b37f-7583-40\u0010b0-9ffc-3f4\u007Fcfa88bf8b@d\u0001omibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId4);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message Id containing only non printable control characters should result in error*/
        try {
            String messageId5 = "\b\u0010\u0030\u007F\u0001";
            backendMessageValidatorObj.validateMessageId(messageId5);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message id more than 255 characters long should result in error*/
        try {
            String messageId6 = "1234567890-123456789-01234567890/1234567890/1234567890.1234567890.123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@domibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId6);
            Assert.fail("Expected exception EBMS_0003 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0003", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id more than 255 characters long should result in error*/

        /*Message id should not be null*/
        try {
            String messageId8 = null;
            backendMessageValidatorObj.validateMessageId(messageId8);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id should not be null*/

        /*Message id should not be blank*/
        try {
            String messageId9 = "     ";
            backendMessageValidatorObj.validateMessageId(messageId9);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id should not be null*/

    }


    @Test
    public void validateRefToMessageId() throws Exception {

        new Expectations() {{
            domibusPropertyProvider.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = MESSAGE_ID_PATTERN;

        }};

        /*Happy Flow No error should occur*/
        try {
            String refTomessageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1);

            String refTomessageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_1);

            String refTomessageId1_2 = "APP-RESPONSE-d8d85972-64fb-4161-a1fb-996aa7a9c39c-DOCUMENT-BUNDLE";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_2);

            String refTomessageId1_3 = "<1234>";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_3);

            String refTomessageId1_4 = "^12^3$4";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_4);

        } catch (EbMS3Exception e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }
        /*Happy Flow No error should occur*/

        /*Message Id with leading and/or trailing whitespaces should throw error*/
        try {
            String refTomessageId2 = "\t\t346ea37f-7583-40b0-9ffc-3f4cfa88bf8b@domibus.eu\t\t";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId2);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id with leading and/or trailing whitespaces should throw error*/


        /*Message Id containing non printable control characters should result in error*/
        try {
            String refTomessageId4 = "346ea\b37f-7583-40\u0010b0-9ffc-3f4\u007Fcfa88bf8b@d\u0001omibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId4);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message Id containing only non printable control characters should result in error*/
        try {
            String refTomessageId5 = "\b\u0010\u0030\u007F\u0001";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId5);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message id more than 255 characters long should result in error*/
        try {
            String refTomessageId6 = "1234567890-123456789-01234567890/1234567890/1234567890.1234567890.123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId6);
            Assert.fail("Expected exception EBMS_0003 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0003", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id more than 255 characters long should result in error*/

        /*Ref To Message id can be null*/
        try {
            backendMessageValidatorObj.validateRefToMessageId(null);

        } catch (EbMS3Exception e2) {
            Assert.fail("RefToMessageId is an optional element and null should be handled!");
        }
        /*Ref To Message id can be null*/

    }

    @Test
    public void testConfigurationNotSpecified() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = null;

            userMessageLogDao.getMessageStatus(anyString);
            result = MessageStatus.NOT_FOUND;
        }};

        /*If the domibus-configuration file does not have the message id format, then message id pattern validation must be skipped. No exception expected*/
        try {
            String refTomessageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateMessageId(refTomessageId1);

            String refTomessageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_1);

        } catch (Exception e1) {
            Assert.fail("When MessageId pattern configuration is not specified, then skip the format validation and no exception is expected!!");
        }
    }

    @Test
    /* Verifies that the initiator and the responder parties are different. */
    public void validatePartiesOk() throws Exception {

        final Party from = new Party();
        from.setName(RED);

        final Party to = new Party();
        to.setName(BLUE);

        backendMessageValidatorObj.validateParties(from, to);

        new Verifications() {{
            Assert.assertNotEquals(from, to);
        }};

    }

    @Test
    /* Verifies that the message is being sent by the same party as the one configured for the sending access point */
    public void validateInitiatorPartyOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(BLUE);

        final Party from = new Party();
        from.setName(BLUE);

        backendMessageValidatorObj.validateInitiatorParty(gatewayParty, from);

        new Verifications() {{
            Assert.assertEquals(gatewayParty, from);
        }};

    }

    @Test
    /* Verifies that the message is NOT being sent by the same party as the one configured for the sending access point */
    public void validateInitiatorPartyNOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(RED);

        final Party from = new Party();
        from.setName(BLUE);

        try {
            backendMessageValidatorObj.validateInitiatorParty(gatewayParty, from);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("does not correspond to the access point's name"));
            assert (ex.getMshRole().equals(MSHRole.SENDING));
        }

    }

    @Test
    /* Verifies that the message is not for the current gateway. */
    public void validateResponderPartyOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(BLUE);

        final Party to = new Party();
        to.setName(RED);

        backendMessageValidatorObj.validateResponderParty(gatewayParty, to);

        new Verifications() {{
            Assert.assertNotEquals(gatewayParty, to);
        }};

    }

    @Test
    /* Verifies that the parties' roles are different. */
    public void validatePartiesRolesOk() throws Exception {

        final Role fromRole = new Role();
        fromRole.setName(INITIATOR_ROLE_NAME);
        fromRole.setValue(INITIATOR_ROLE_VALUE);

        final Role toRole = new Role();
        toRole.setName(RESPONDER_ROLE_NAME);
        toRole.setValue(RESPONDER_ROLE_VALUE);

        backendMessageValidatorObj.validatePartiesRoles(fromRole, toRole);

        new Verifications() {{
            Assert.assertNotEquals(fromRole, toRole);
        }};

    }

    @Test
    /* Verifies that the parties' roles are the same. */
    public void validatePartiesRolesNOk() throws Exception {

        final Role fromRole = new Role();
        fromRole.setName(INITIATOR_ROLE_NAME);
        fromRole.setValue(INITIATOR_ROLE_VALUE);


        final Role toRole = new Role();
        toRole.setName(INITIATOR_ROLE_NAME);
        toRole.setValue(INITIATOR_ROLE_VALUE);

        try {
            backendMessageValidatorObj.validatePartiesRoles(fromRole, toRole);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("The initiator party's role is the same as the responder party's one"));
        }

    }

    @Test
    public void validateAgreementRef_OK() {
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("AgreementRefPMode");
        agreementRef.setType("AgreementRefType");
        agreementRef.setValue("AgreementRefValue");

        try {
            backendMessageValidatorObj.validateAgreementRef(agreementRef);
        } catch (EbMS3Exception e) {
            Assert.fail("Exception was not expected here!");
        }
    }

    @Test
    public void validateAgreementRef_NullCheck(@Injectable AgreementRef agreementRef) {
        try {
            backendMessageValidatorObj.validateAgreementRef(agreementRef);
        } catch (EbMS3Exception e) {
            Assert.fail("Exception was not expected here as agreementRef is optional and can be null!");
        }
    }

    @Test
    public void validateAgreementRef_PmodeTooLong() throws EbMS3Exception {
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        agreementRef.setType("AgreementRefType");
        agreementRef.setValue("AgreementRefValue");

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("AgreementRef Pmode is too long (over 255 characters)");

        backendMessageValidatorObj.validateAgreementRef(agreementRef);
    }

    @Test
    public void validateAgreementRef_TypeTooLong() throws EbMS3Exception {
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("AgreementRefPMode");
        agreementRef.setType("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        agreementRef.setValue("AgreementRefValue");

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("AgreementRef Type is too long (over 255 characters)");

        backendMessageValidatorObj.validateAgreementRef(agreementRef);
    }

    @Test
    public void validateAgreementRef_ValueTooLong() throws EbMS3Exception {
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setPmode("AgreementRefPMode");
        agreementRef.setType("AgreementRefType");
        agreementRef.setValue("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("AgreementRef Value is too long (over 255 characters)");

        backendMessageValidatorObj.validateAgreementRef(agreementRef);
    }


    @Test
    public void validateConversationId_ValueTooLong() throws EbMS3Exception {
        String conversationId = StringUtils.repeat("01234", 51) + "1";

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("ConversationId is too long (over 255 characters)");

        backendMessageValidatorObj.validateConversationId(conversationId);
    }

    @Test
    public void validateConversationId_BlankValueTooLong() throws EbMS3Exception {
        String conversationId = StringUtils.repeat(" ", 256);

        backendMessageValidatorObj.validateConversationId(conversationId);
    }

    @Test
    public void validateConversationId_Value255Long() throws EbMS3Exception {
        String conversationId = StringUtils.repeat("01234", 51);
        backendMessageValidatorObj.validateConversationId(conversationId);
    }

    @Test
    public void validateConversationId_ValueNull() throws EbMS3Exception {
        ExpectedException.none();
        backendMessageValidatorObj.validateConversationId(null);
    }

    @Test
    public void validateUserMessageForPmodeMatch_ValidationsOK() throws DuplicateMessageException, EbMS3Exception {
        UserMessage userMessage = createUserMessage();
        userMessage.getMessageInfo().setMessageId(MESS_ID);
        new Expectations() {{
            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;
        }};
        ExpectedException.none();
        backendMessageValidatorObj.validateUserMessageForPmodeMatch(userMessage, MSHRole.SENDING);
    }

    @Test
    public void validateUserMessageForPmodeMatch_UserMessageNull() throws DuplicateMessageException, EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory header metadata UserMessage is not provided.");
        backendMessageValidatorObj.validateUserMessageForPmodeMatch(null, MSHRole.SENDING);
    }

    @Test
    public void validateMessageInfo_MessageInfoNull() throws DuplicateMessageException, EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory header metadata UserMessage/MessageInfo is not provided.");
        backendMessageValidatorObj.validateMessageInfo(null);
    }

    @Test
    public void validatePartyInfoForPModeMatch_PartyInfoNull() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field PartyInfo is not provided.");
        backendMessageValidatorObj.validatePartyInfoForPModeMatch(null, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_FromNull() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field PartyInfo/From is not provided.");
        backendMessageValidatorObj.validateFromPartyId(null, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_EmptyFromParties() throws EbMS3Exception {
        From from = new From();
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field From PartyId is not provided.");
        backendMessageValidatorObj.validateFromPartyId(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_BlankFromPartId() throws EbMS3Exception {
        From from = new From();
        PartyId fromPartyId = new PartyId();
        fromPartyId.setValue("        ");
        fromPartyId.setType("        ");
        from.getPartyId().add(fromPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field From PartyId is not provided.");
        backendMessageValidatorObj.validateFromPartyId(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_FromPartIdTooLong() throws EbMS3Exception {
        From from = new From();
        PartyId fromPartyId = new PartyId();
        fromPartyId.setValue(StringUtils.repeat("X", 256));
        fromPartyId.setType("        ");
        from.getPartyId().add(fromPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("From PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateFromPartyId(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_FromPartIdTypeBlank() throws EbMS3Exception {
        From from = new From();
        PartyId fromPartyId = new PartyId();
        fromPartyId.setValue(StringUtils.repeat("X", 255));
        fromPartyId.setType("        ");
        from.getPartyId().add(fromPartyId);
        ExpectedException.none();
        backendMessageValidatorObj.validateFromPartyId(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromPartyId_FromPartIdTypeTooLong() throws EbMS3Exception {
        From from = new From();
        PartyId fromPartyId = new PartyId();
        fromPartyId.setValue(StringUtils.repeat("X", 255));
        fromPartyId.setType(StringUtils.repeat("X", 256));
        from.getPartyId().add(fromPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("From PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateFromPartyId(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromRole_FromNull() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field PartyInfo/From is not provided.");
        backendMessageValidatorObj.validateFromRole(null, MSHRole.SENDING);

    }

    @Test
    public void validateFromRole_FromRoleBlank() throws EbMS3Exception {
        From from = new From();
        from.setRole("   ");
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field From Role is not provided.");
        backendMessageValidatorObj.validateFromRole(from, MSHRole.SENDING);

    }

    @Test
    public void validateFromRole_FromRoleTooLong() throws EbMS3Exception {
        From from = new From();
        from.setRole(StringUtils.repeat("X", 256));
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("From Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateFromRole(from, MSHRole.SENDING);

    }

    @Test
    public void validateToPartyIdForPModeMatch_ToNull() throws EbMS3Exception {
        ExpectedException.none();
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(null, MSHRole.SENDING);
    }

    @Test
    public void validateToPartyIdForPModeMatch_EmptyToParties() throws EbMS3Exception {
        To to = new To();
        ExpectedException.none();
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(to, MSHRole.SENDING);
    }

    @Test
    public void validateToPartyIdForPModeMatch_BlankToPartId() throws EbMS3Exception {
        To to = new To();
        PartyId toPartyId = new PartyId();
        toPartyId.setValue("        ");
        toPartyId.setType("        ");
        to.getPartyId().add(toPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field To PartyId is not provided.");
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateToPartyIdForPModeMatch_ToPartIdTooLong() throws EbMS3Exception {
        To to = new To();
        PartyId toPartyId = new PartyId();
        toPartyId.setValue(StringUtils.repeat("X", 256));
        toPartyId.setType("        ");
        to.getPartyId().add(toPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("To PartyId" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateToPartyIdForPModeMatch_ToPartIdTypeBlank() throws EbMS3Exception {
        To to = new To();
        PartyId toPartyId = new PartyId();
        toPartyId.setValue(StringUtils.repeat("X", 255));
        toPartyId.setType("        ");
        to.getPartyId().add(toPartyId);
        ExpectedException.none();
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateToPartyIdForPModeMatch_ToPartIdTypeTooLong() throws EbMS3Exception {
        To to = new To();
        PartyId toPartyId = new PartyId();
        toPartyId.setValue(StringUtils.repeat("X", 255));
        toPartyId.setType(StringUtils.repeat("X", 256));
        to.getPartyId().add(toPartyId);
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("To PartyIdType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateToPartyIdForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateToRole_ToNull() throws EbMS3Exception {
        ExpectedException.none();
        backendMessageValidatorObj.validateToRoleForPModeMatch(null, MSHRole.SENDING);
    }

    @Test
    public void validateToRole_ToRoleBlank() throws EbMS3Exception {
        To to = new To();
        to.setRole("   ");
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field To Role is not provided.");
        backendMessageValidatorObj.validateToRoleForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateToRole_ToRoleTooLong() throws EbMS3Exception {
        To to = new To();
        to.setRole(StringUtils.repeat("X", 256));
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("To Role" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateToRoleForPModeMatch(to, MSHRole.SENDING);

    }

    @Test
    public void validateCollaborationInfo_CollaborationInfoNull() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field UserMessage/CollaborationInfo is not provided.");
        backendMessageValidatorObj.validateCollaborationInfo(null);
    }

    @Test
    public void validateService_ServiceBlank() throws EbMS3Exception {
        Service service = new Service();
        service.setValue("\t");
        service.setType("\t");
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field Service is not provided.");
        backendMessageValidatorObj.validateService(service);
    }

    @Test
    public void validateService_ServiceTooLong() throws EbMS3Exception {
        Service service = new Service();
        service.setValue(StringUtils.repeat("X", 256));
        service.setType("\t");
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Service" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateService(service);
    }

    @Test
    public void validateService_ServiceTypeBlank() throws EbMS3Exception {
        Service service = new Service();
        service.setValue(StringUtils.repeat("X", 255));
        service.setType("\t");
        ExpectedException.none();
        backendMessageValidatorObj.validateService(service);
    }

    @Test
    public void validateService_ServiceTypeTooLong() throws EbMS3Exception {
        Service service = new Service();
        service.setValue(StringUtils.repeat("X", 255));
        service.setType(StringUtils.repeat("X", 256));
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("ServiceType" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateService(service);
    }

    @Test
    public void validateAction_ActionBlank() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field Action is not provided.");
        backendMessageValidatorObj.validateAction("\t");

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Mandatory field Action is not provided.");
        backendMessageValidatorObj.validateAction(null);
    }

    @Test
    public void validateAction_ActionTooLong() throws EbMS3Exception {
        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Action" + ERROR_MSG_STRING_LONGER_THAN_DEFAULT_STRING_LENGTH);
        backendMessageValidatorObj.validateAction(StringUtils.repeat("X", 256));
    }
}

class MessageIdPatternRetriever extends DefaultHandler {

    private static final String PROP_KEY = "prop";
    private static String MessageIdPattern;
    private boolean bHasMessageIdPattern;

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (PROP_KEY.equalsIgnoreCase(qName) && BackendMessageValidator.KEY_MESSAGEID_PATTERN.equalsIgnoreCase(atts.getValue("key"))) {
            bHasMessageIdPattern = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (bHasMessageIdPattern) {
            MessageIdPattern = new String(ch, start, length);
            bHasMessageIdPattern = false;
        }
    }

    public String getMessageIdPattern() {
        return MessageIdPattern;
    }
}
