package eu.domibus.logging;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusLoggingEventSenderTest {

    @Tested
    DomibusLoggingEventSender domibusLoggingEventSender;

    @Test
    public void test_getLogMessage_MultipartTrue(final @Mocked LogEvent logEvent) {
        final String payload = "--uuid:1bf7dc24-98f3-405b-83f0-4ed885db1bdf\n" +
                "Content-Type: application/soap+xml; charset=UTF-8\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: <root.message@cxf.apache.org>\n" +
                "\n" +
                "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Header><eb:Messaging xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" env:mustUnderstand=\"true\" wsu:Id=\"_1a30190e6c3bec22b8ce709301aff34a7300358d21b809bdfb97fc03a28773476\"><eb:UserMessage mpc=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC\"><eb:MessageInfo><eb:Timestamp>2019-06-12T14:50:49.000Z</eb:Timestamp><eb:MessageId>548e6025-8c58-4fdb-86c9-8b200164888c@domibus.eu</eb:MessageId></eb:MessageInfo><eb:PartyInfo><eb:From><eb:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-blue</eb:PartyId><eb:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator</eb:Role></eb:From><eb:To><eb:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-red</eb:PartyId><eb:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder</eb:Role></eb:To></eb:PartyInfo><eb:CollaborationInfo><eb:Service type=\"tc1\">bdx:noprocess</eb:Service><eb:Action>TC1Leg1</eb:Action><eb:ConversationId>6c744b70-d47b-40f0-8854-e9d8e28579b0@domibus.eu</eb:ConversationId></eb:CollaborationInfo><eb:MessageProperties><eb:Property name=\"originalSender\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1</eb:Property><eb:Property name=\"finalRecipient\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4</eb:Property></eb:MessageProperties><eb:PayloadInfo><eb:PartInfo href=\"cid:message\"><eb:PartProperties><eb:Property name=\"MimeType\">text/xml</eb:Property><eb:Property name=\"CompressionType\">application/gzip</eb:Property></eb:PartProperties></eb:PartInfo></eb:PayloadInfo></eb:UserMessage></eb:Messaging><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" env:mustUnderstand=\"true\"><xenc:EncryptedKey xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"EK-94531588-f5e4-49ef-98a5-bbe412509e96\"><xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#rsa-oaep\"><ds:DigestMethod xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><xenc11:MGF xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" Algorithm=\"http://www.w3.org/2009/xmlenc11#mgf1sha256\"/></xenc:EncryptionMethod><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><wsse:SecurityTokenReference><wsse:KeyIdentifier EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">7AJeAnEL5EE5l3lLc+EoFpOfJCo=</wsse:KeyIdentifier></wsse:SecurityTokenReference></ds:KeyInfo><xenc:CipherData><xenc:CipherValue>woWcrCWPWvCk42F70UrGbSW4CVuf207TndLbrd/pXkTvtvo22qwO48vSApmmdAFmoqRlwP7nohTC4JN5J5aLiaOmbPE3Yu7XBQA5EeC97wVGIKnZw7PgQxQuTAo9ray4Wuw7zxCOWbPE3HGy7scVhcnpX3sjZ95F2CV6HoUe8B72LVg4Knbjvq/2fq/thvmvbA+5/QhjdE4nQtDXfVRAFB9GSIRt4dh8kijuJGLgPq8CyUFRhuFAu/SU517w5U+Yxrl0L8mUz53SbGe6Qg2joKy8mzboP1grJwPXL7IDHkAZZR+osTLfKO8NbftfAi/ttNz8xxLNLi2z52H583BW6g==</xenc:CipherValue></xenc:CipherData><xenc:ReferenceList><xenc:DataReference URI=\"#ED-9a3ab02a-9bc8-4f34-afd5-bc06b4376d20\"/></xenc:ReferenceList></xenc:EncryptedKey><xenc:EncryptedData xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"ED-9a3ab02a-9bc8-4f34-afd5-bc06b4376d20\" MimeType=\"application/gzip\" Type=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Content-Only\"><xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#aes128-gcm\"/><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><wsse:SecurityTokenReference xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsse11=\"http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd\" wsse11:TokenType=\"http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#EncryptedKey\"><wsse:Reference URI=\"#EK-94531588-f5e4-49ef-98a5-bbe412509e96\"/></wsse:SecurityTokenReference></ds:KeyInfo><xenc:CipherData><xenc:CipherReference URI=\"cid:message\"><xenc:Transforms><ds:Transform xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Algorithm=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Ciphertext-Transform\"/></xenc:Transforms></xenc:CipherReference></xenc:CipherData></xenc:EncryptedData><ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Id=\"SIG-b4d3f2df-5778-4551-bdcc-9ba9053a0600\"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"><ec:InclusiveNamespaces xmlns:ec=\"http://www.w3.org/2001/10/xml-exc-c14n#\" PrefixList=\"env\"/></ds:CanonicalizationMethod><ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/><ds:Reference URI=\"#_2a30190e6c3bec22b8ce709301aff34a7300358d21b809bdfb97fc03a28773476\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>OKRki7TIvjbyXiULnNviWRgIdwVB/GvotFr/BIGNV0k=</ds:DigestValue></ds:Reference><ds:Reference URI=\"#_1a30190e6c3bec22b8ce709301aff34a7300358d21b809bdfb97fc03a28773476\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>ZAr7xvFjRbw62D9Pr0DCd+VPYmAOdPMRQbuKUzIR8qs=</ds:DigestValue></ds:Reference><ds:Reference URI=\"cid:message\"><ds:Transforms><ds:Transform Algorithm=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Content-Signature-Transform\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>EsCW4yjyIQ0WVh8swWQx16Nt84LBP7ufBWcyBePJEE8=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>Ppw4hHZ0bAxvvNr/LonK0nCIv9bhz0SsajuYYamVdkHzukL/fjOAJgclqmIeM4TUiOjaC1sRDvNAVkJxZQPfWiuFcYFEytTqZJWws2OgvULylgrTjjDvRSw6VzV+wRTWPLyznurq/x4/aBqTiRRvPRD7jnrVWRV3eJqzYOpEQEWM1T/HELDI6cggYhRkGreNWVWMbMuvzC4DJ0k5qShlXD63yg1v/vQNtRQKG6fbLj5g7iD+WiGPrMzJPyPyMt38sqDhOy9UrNEbrK8mAjf6QQb3nwpscrX9suePp4XNwSZXW4DZgw1Ssp7g8dVdVib1DeRmQnu4AK95glxWVORUOw==</ds:SignatureValue><ds:KeyInfo Id=\"KI-46ecc4ff-0cb2-4fd1-90a0-0f1a3ed959c7\"><wsse:SecurityTokenReference xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"STR-e57c6a25-b040-4955-8fbd-dba539b11ff2\"><wsse:KeyIdentifier EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">HPqVjsgDN900CCgLwAvG03x+R2I=</wsse:KeyIdentifier></wsse:SecurityTokenReference></ds:KeyInfo></ds:Signature></wsse:Security></env:Header><env:Body xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"_2a30190e6c3bec22b8ce709301aff34a7300358d21b809bdfb97fc03a28773476\"/></env:Envelope>\n" +
                "--uuid:1bf7dc24-98f3-405b-83f0-4ed885db1bdf\n" +
                "Content-Type: application/octet-stream\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: <message>\n" +
                "\n" +
                "ȹ_��F+�e;�\u0013OG\u001F�ɬ� \u000Ed�ΐ'�δ��,�TJ�A�7jY�������]\u0016������;��=�ګ�s��P\u001Bҳ\u0013\u0003�@�ϛ��Ļ\t\u0005/�Ah�:T����;�J\n" +
                "�\u0012\n" +
                "--uuid:1bf7dc24-98f3-405b-83f0-4ed885db1bdf--";

        new Expectations() {{
            logEvent.isMultipartContent();
            result = true;

            logEvent.getPayload();
            result = payload;



        }};

        //tested method
        domibusLoggingEventSender.getLogMessage(logEvent);

        new Verifications() {{
            final String payloadActual;
            logEvent.setPayload(payloadActual = withCapture());
            Assert.assertNotNull(payloadActual);
            Assert.assertTrue(payloadActual.split(DomibusLoggingEventSender.CONTENT_TYPE).length == 2);
        }};
    }
}