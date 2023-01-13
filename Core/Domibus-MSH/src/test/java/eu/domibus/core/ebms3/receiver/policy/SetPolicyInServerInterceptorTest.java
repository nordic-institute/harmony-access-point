package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.receiver.leg.ServerInMessageLegConfigurationFactory;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.SoapService;
import eu.domibus.core.message.TestMessageValidator;
import eu.domibus.core.message.UserMessageErrorCreator;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.core.util.SecurityProfileService;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author Catalin Enache, Soumya Chandran
 * @since 4.2
 */
@SuppressWarnings("ConstantConditions")
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
    Ebms3Converter ebms3Converter;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected DomibusVersionService domibusVersionService;

    @Injectable
    ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    @Injectable
    UserMessageErrorCreator userMessageErrorCreator;

    @Injectable
    SecurityProfileService securityProfileService;

    @Test
    public void processPluginNotification(final @Injectable EbMS3Exception ebMS3Exception,
                                          final @Injectable LegConfiguration legConfiguration,
                                          final @Injectable Ebms3Messaging messaging,
                                          final @Injectable Ebms3UserMessage ebms3UserMessage,
                                          final @Injectable UserMessage userMessage,
                                          final @Injectable PartInfo partInfo,
                                          final @Injectable ErrorResult errorResult,
                                          final @Injectable TestMessageValidator testMessageValidator,
                                          final @Injectable UserMessageErrorCreator userMessageErrorCreator) {
        List<PartInfo> partInfos = Arrays.asList(partInfo);

        new Expectations(setPolicyInServerInterceptor) {{
            messaging.getUserMessage();
            result = ebms3UserMessage;

            ebms3Converter.convertFromEbms3(ebms3UserMessage);
            result = userMessage;

            testMessageValidator.checkTestMessage(userMessage);
            result = false;

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;
        }};

        //tested method
        setPolicyInServerInterceptor.processPluginNotification(ebMS3Exception, legConfiguration, messaging);

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(userMessage, errorResult);
        }};
    }

    @Test
    public void logIncomingMessaging(final @Injectable SoapMessage soapMessage,
                                     final @Injectable TestMessageValidator testMessageValidator) throws Exception {

        //tested method
        setPolicyInServerInterceptor.logIncomingMessaging(soapMessage);

        new Verifications() {{
            soapService.getMessagingAsRAWXml(soapMessage);
        }};
    }

    @Test
    public void handleMessage(@Injectable SoapMessage message,
                              @Injectable HttpServletResponse response,
                              final @Injectable TestMessageValidator testMessageValidator) throws JAXBException, IOException, EbMS3Exception {

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
                                               @Injectable HttpServletResponse response,
                                               final @Injectable TestMessageValidator testMessageValidator
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
    public void handleMessageEbMS3Exception(@Injectable SoapMessage message,
                                            @Injectable HttpServletResponse response,
                                            @Injectable Messaging messaging,
                                            final @Injectable TestMessageValidator testMessageValidator) throws JAXBException, IOException, EbMS3Exception {

        new Expectations() {{
            soapService.getMessage(message);
            result = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("no valid security policy found")
                    .build();
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
