package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.leg.ServerInMessageLegConfigurationFactory;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.SoapService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.ebms3.common.model.Messaging;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * @author Catalin Enache, Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class SetPolicyInServerInterceptorTest {

    @Tested
    SetPolicyInServerInterceptor setPolicyInServerInterceptor;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    @Injectable
    SoapService soapService;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected DomibusVersionService domibusVersionService;

    @Injectable
    ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    @Test
    public void processPluginNotification(final @Mocked EbMS3Exception ebMS3Exception,
                                          final @Mocked LegConfiguration legConfiguration,
                                          final @Mocked Messaging messaging) {

        new Expectations(setPolicyInServerInterceptor) {{
            userMessageHandlerService.checkTestMessage(messaging.getUserMessage());
            result = false;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;
        }};

        //tested method
        setPolicyInServerInterceptor.processPluginNotification(ebMS3Exception, legConfiguration, messaging);

        new FullVerifications(setPolicyInServerInterceptor, backendNotificationService) {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(ebMS3Exception));
        }};
    }

    @Test
    public void processPluginNotificationEmptyUserMessage(final @Injectable EbMS3Exception ebMS3Exception,
                                                          final @Injectable LegConfiguration legConfiguration,
                                                          final @Injectable Messaging messaging) {

        new Expectations(setPolicyInServerInterceptor) {{
            messaging.getUserMessage();
            result = null;
        }};

        //tested method
        setPolicyInServerInterceptor.processPluginNotification(ebMS3Exception, legConfiguration, messaging);

        new FullVerifications(setPolicyInServerInterceptor, backendNotificationService) {{
        }};
    }

    @Test
    public void logIncomingMessaging(final @Mocked SoapMessage soapMessage) throws Exception {

        //tested method
        setPolicyInServerInterceptor.logIncomingMessaging(soapMessage);

        new Verifications() {{
            soapService.getMessagingAsRAWXml(soapMessage);
        }};
    }

    @Test
    public void handleMessage(@Injectable SoapMessage message, @Injectable HttpServletResponse response) throws JAXBException, IOException, EbMS3Exception {

        setPolicyInServerInterceptor.handleMessage(message);

        new Verifications() {{
            soapService.getMessage(message);
            times = 1;
            policyService.parsePolicy("policies" + File.separator + anyString);
            times = 1;
        }};
    }

    @Test(expected = Fault.class)
    public void handleMessageThrowsIOException(@Injectable SoapMessage message,
                                               @Injectable HttpServletResponse response
    ) throws JAXBException, IOException, EbMS3Exception {


        new Expectations() {{
            soapService.getMessage(message);
            result = new IOException();

        }};

        setPolicyInServerInterceptor.handleMessage(message);

        new FullVerifications() {{
            soapService.getMessage(message);
            policyService.parsePolicy("policies" + File.separator + anyString);
            setPolicyInServerInterceptor.setBindingOperation(message);
        }};
    }

    @Test(expected = Fault.class)
    public void handleMessageEbMS3Exception(@Injectable SoapMessage message, @Injectable HttpServletResponse response, @Injectable Messaging messaging) throws JAXBException, IOException, EbMS3Exception {

        new Expectations() {{
            soapService.getMessage(message);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "no valid security policy found", null, null);
        }};
        setPolicyInServerInterceptor.handleMessage(message);

        new Verifications() {{
            soapService.getMessage(message);
            times = 1;
            policyService.parsePolicy("policies" + File.separator + anyString);
            times = 1;
            setPolicyInServerInterceptor.setBindingOperation(message);
            times = 1;
        }};
    }
}