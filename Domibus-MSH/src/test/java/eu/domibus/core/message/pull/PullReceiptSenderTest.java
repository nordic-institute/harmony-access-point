package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.core.ebms3.sender.MSHDispatcher;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author idragusa
 * @since 4.1
 */
@RunWith(JMockit.class)
public class PullReceiptSenderTest {

    @Injectable
    private MSHDispatcher mshDispatcher;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Tested
    private PullReceiptSender pullReceiptSender;

    @Autowired
    protected SoapUtil soapUtil;

    static MessageFactory messageFactory = null;

    @BeforeClass
    public static void init() throws SOAPException {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    }

    @Test
    public void sendReceiptOKTest(@Mocked SOAPMessage soapMessage, @Mocked String endpoint, @Mocked Policy policy, @Mocked LegConfiguration legConfiguration, @Mocked String pModeKey, @Mocked String messsageId, @Mocked String domainCode) throws EbMS3Exception {
        new Expectations() {{
            mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
            result = null; // expected response for a pull receipt is null
        }};

        pullReceiptSender.sendReceipt(soapMessage, endpoint, policy, legConfiguration, pModeKey, messsageId, domainCode);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
            times = 1;
        }};
    }

    @Test(expected = EbMS3Exception.class)
    public void sendReceiptNotSignalTest(@Mocked SOAPMessage soapMessage, @Mocked String endpoint, @Mocked Policy policy, @Mocked LegConfiguration legConfiguration, @Mocked String pModeKey, @Mocked String messsageId,
                                         @Mocked String domainCode) throws Exception {
        messsageId = "123123123123@domibus.eu";
        SignalMessage signalMessage = new SignalMessage();
        Error error = new Error();
        error.setErrorCode(ErrorCode.EBMS_0001.getErrorCodeName());
        error.setErrorDetail("Some details about the test error");
        error.setRefToMessageInError(messsageId);
        signalMessage.getError().add(error);
        Messaging messaging = new Messaging();
        messaging.setSignalMessage(signalMessage);

        new Expectations() {{
            mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
            result = messageFactory.createMessage();

            messageUtil.getMessage((SOAPMessage) any);
            result = messaging;
        }};

        pullReceiptSender.sendReceipt(soapMessage, endpoint, policy, legConfiguration, pModeKey, messsageId, domainCode);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
            times = 1;
        }};
    }

    @Test
    public void sendReceiptNullSignalTest(@Mocked SOAPMessage soapMessage, @Mocked String endpoint, @Mocked Policy policy, @Mocked LegConfiguration legConfiguration, @Mocked String pModeKey, @Mocked String messsageId,
                                          @Mocked String domainCode) throws Exception {
        Messaging messaging = new Messaging();
        messaging.setSignalMessage(null); // test it doesn't crash when null

        new Expectations() {{
            mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
            result = messageFactory.createMessage();

            messageUtil.getMessage((SOAPMessage) any);
            result = messaging;
        }};

        pullReceiptSender.sendReceipt(soapMessage, endpoint, policy, legConfiguration, pModeKey, messsageId, domainCode);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
            times = 1;
        }};
    }

}
