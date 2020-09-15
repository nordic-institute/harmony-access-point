package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.SoapService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.ebms3.common.model.Messaging;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
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
}