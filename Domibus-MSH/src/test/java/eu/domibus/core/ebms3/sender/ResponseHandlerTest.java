package eu.domibus.core.ebms3.sender;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.common.ErrorCode;
import eu.domibus.api.model.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDefaultService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import static org.hamcrest.core.Is.is;

/**
 * @author François Gautier
 * @since 4.2
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class ResponseHandlerTest {

    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String DETAIL = "Problem occurred during marshalling";
    public static final DomibusDateTimeException CAUSE = new DomibusDateTimeException("TEST");
    @Tested
    private ResponseHandler responseHandler;
    @Injectable
    private SignalMessageLogDefaultService signalMessageLogDefaultService;
    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;
    @Injectable
    private NonRepudiationService nonRepudiationService;
    @Injectable
    private SignalMessageDao signalMessageDao;
    @Injectable
    protected MessageUtil messageUtil;
    @Injectable
    private MessagingDao messagingDao;
    @Injectable
    private ErrorLogDao errorLogDao;

    @Mocked
    private SOAPMessage soapMessage;
    @Mocked
    private Ebms3Messaging ebms3Messaging;
    @Mocked
    private Ebms3SignalMessage ebms3SignalMessage;
    @Mocked
    private ResponseHandler.ResponseStatus responseStatus;

    @Injectable
    Ebms3Converter ebms3Converter;

    @Test
    public void verifyResponse_ok() throws EbMS3Exception, SOAPException {

        new Expectations(responseHandler) {{
            messageUtil.getMessagingWithDom(soapMessage);
            result = ebms3Messaging;

            ebms3Messaging.getSignalMessage();
            result = ebms3SignalMessage;

            responseHandler.getResponseStatus(ebms3SignalMessage);
            result = responseStatus;

        }};
        ResponseResult responseResult = responseHandler.verifyResponse(soapMessage, null);

        Assert.assertNotNull(responseResult);
        Assert.assertThat(responseResult.getResponseStatus(), is(responseStatus));

        new FullVerifications() {
        };
    }

    @Test
    public void verifyResponse_exception() throws EbMS3Exception, SOAPException {

        new Expectations(responseHandler) {{
            messageUtil.getMessagingWithDom(soapMessage);
            result = CAUSE;
        }};
        try {
            responseHandler.verifyResponse(soapMessage, MESSAGE_ID);
            Assert.fail();
        } catch (EbMS3Exception e) {
           Assert.assertThat(e.getErrorCode(), is(ErrorCode.EbMS3ErrorCode.EBMS_0004));
           Assert.assertThat(e.getErrorDetail(), is(DETAIL));
           Assert.assertThat(e.getRefToMessageId(), is(MESSAGE_ID));
           Assert.assertThat(e.getMshRole(), is(MSHRole.SENDING));
           Assert.assertThat(e.getCause(), is(CAUSE));
        }

        new FullVerifications() {
        };
    }
}