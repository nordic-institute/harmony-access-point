package eu.domibus.plugin.jms;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class BackendJMSQueueServiceTest {

    @Tested
    BackendJMSQueueService backendJMSQueueService;

    @Injectable
    DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    DomainContextExtService domainContextExtService;

    @Injectable
    JmsPluginPropertyManager jmsPluginPropertyManager;

    @Injectable
    MessageRetriever messageRetriever;

    @Test
    public void getJMSQueue(@Injectable Submission submission) throws MessageNotFoundException {
        String messageId = "123";
        String service = "myService";
        String action = "myAction";
        String defaultQueueProperty = "domibus.defaultQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";

        new Expectations(backendJMSQueueService) {{
            messageRetriever.browseMessage(messageId);
            result = submission;

            submission.getService();
            result = service;

            submission.getAction();
            result = action;

            backendJMSQueueService.getJMSQueue((QueueContext) any, anyString, anyString);
        }};

        backendJMSQueueService.getJMSQueue(messageId, defaultQueueProperty, routingQueuePrefixProperty);

        new Verifications() {{
            QueueContext queueContext = null;
            backendJMSQueueService.getJMSQueue(queueContext = withCapture(), defaultQueueProperty, routingQueuePrefixProperty);

            Assert.assertEquals(messageId, queueContext.getMessageId());
            Assert.assertEquals(service, queueContext.getService());
            Assert.assertEquals(action, queueContext.getAction());
        }};
    }

    @Test
    public void getQueueValueWithRouting(@Injectable QueueContext queueContext,
                                         @Injectable DomainDTO domainDTO) throws MessageNotFoundException {
        String messageId = "123";
        String defaultQueueProperty = "domibus.defaultQueue";
        String routingQueueValue = "domibus.myQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";


        List<String> routingQueuePrefixNameList = new ArrayList<>();
        routingQueuePrefixNameList.add("routing.rule1");

        new Expectations(backendJMSQueueService) {{
            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            domibusPropertyExtService.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
            result = routingQueueValue;
        }};

        String queueValue = backendJMSQueueService.getQueueValue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
        Assert.assertEquals(queueValue, routingQueueValue);
    }

    @Test
    public void getQueueValueWithDefaultQueue(@Injectable QueueContext queueContext,
                                              @Injectable DomainDTO domainDTO) throws MessageNotFoundException {
        String messageId = "123";
        String defaultQueueProperty = "domibus.defaultQueue";
        String defaultQueueValue = "domibus.myQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";


        List<String> routingQueuePrefixNameList = new ArrayList<>();
        routingQueuePrefixNameList.add("routing.rule1");

        new Expectations(backendJMSQueueService) {{
            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            domibusPropertyExtService.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
            result = null;

            domibusPropertyExtService.getProperty(domainDTO, defaultQueueProperty);
            result = defaultQueueValue;
        }};

        String queueValue = backendJMSQueueService.getQueueValue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
        Assert.assertEquals(queueValue, defaultQueueValue);
    }

    @Test
    public void getRoutingQueueValue(@Injectable QueueContext queueContext,
                                     @Injectable DomainDTO domainDTO) {
        List<String> routingQueuePrefixNameList = new ArrayList<>();
        String prefix = "routing.rule1";
        routingQueuePrefixNameList.add(prefix);
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";
        String queueValue = "domibus.myQueue";

        new Expectations(backendJMSQueueService) {{
            backendJMSQueueService.getRoutingQueue(routingQueuePrefixProperty, prefix, queueContext, domainDTO);
            result = queueValue;
        }};

        String routingQueueValue = backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
        Assert.assertEquals(queueValue, routingQueueValue);
    }

    @Test
    public void getRoutingQueue(@Injectable QueueContext queueContext,
                                @Injectable DomainDTO domainDTO) {


        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";
        String routingQueuePrefixName = "queue";

        String queueProperty = "domibus.queue.property";
        String queueValue = "domibus.myQueue";

        String serviceProperty = "serviceProperty";
        String serviceValue = "serviceValue";

        String actionProperty = "actionProperty";
        String actionValue = "actionValue";

        new Expectations(backendJMSQueueService) {{
            backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "service");
            result = serviceProperty;

            domibusPropertyExtService.getProperty(domainDTO, serviceProperty);
            result = serviceValue;

            backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "action");
            result = actionProperty;

            domibusPropertyExtService.getProperty(domainDTO, actionProperty);
            result = actionValue;

            backendJMSQueueService.matchesQueueContext(serviceValue, actionValue, queueContext);
            result = true;

            backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "queue");
            result = queueProperty;

            domibusPropertyExtService.getProperty(domainDTO, queueProperty);
            result = queueValue;
        }};

        String routingQueue = backendJMSQueueService.getRoutingQueue(routingQueuePrefixProperty, routingQueuePrefixName, queueContext, domainDTO);
        Assert.assertEquals(routingQueue, queueValue);
    }

    @Test
    public void getQueuePropertyName() {
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing";
        String routingQueuePrefixName = "rule1";

        String suffix = "queue";
        String queue = backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, suffix);
        Assert.assertEquals(routingQueuePrefixProperty + "." + routingQueuePrefixName + "." + suffix, queue);
    }

    @Test
    public void matchesSubmissionWithServiceAndAction(@Injectable QueueContext queueContext) {
        String service = "serviceValue";
        String action = "actionValue";

        new Expectations() {{
            queueContext.getService();
            result = service;

            queueContext.getAction();
            result = action;
        }};

        Assert.assertTrue(backendJMSQueueService.matchesQueueContext(service, action, queueContext));
    }

    @Test
    public void matchesSubmissionWithSameServiceAndDifferentAction(@Injectable QueueContext queueContext) {
        String service = "serviceValue";
        String action = "actionValue";

        new Expectations() {{
            queueContext.getService();
            result = service;

            queueContext.getAction();
            result = action;
        }};

        Assert.assertFalse(backendJMSQueueService.matchesQueueContext(service, "myAction", queueContext));
    }

    @Test
    public void matchesSubmissionWithServiceAndNoAction(@Injectable QueueContext queueContext) {
        String service = "serviceValue";

        new Expectations() {{
            queueContext.getService();
            result = service;
        }};

        Assert.assertTrue(backendJMSQueueService.matchesQueueContext(service, null, queueContext));

        new Verifications() {{
            queueContext.getAction();
            times = 0;
        }};
    }

    @Test
    public void matchesSubmissionWithDifferentServiceAndNoAction(@Injectable QueueContext queueContext) {
        String service = "serviceValue";

        new Expectations() {{
            queueContext.getService();
            result = service;
        }};

        Assert.assertFalse(backendJMSQueueService.matchesQueueContext("differentService", null, queueContext));

        new Verifications() {{
            queueContext.getAction();
            times = 0;
        }};
    }

    @Test
    public void matchesSubmissionWithNoServiceAndAction(@Injectable QueueContext queueContext) {
        String action = "actionValue";

        new Expectations() {{
            queueContext.getAction();
            result = action;
        }};

        Assert.assertTrue(backendJMSQueueService.matchesQueueContext(null, action, queueContext));

        new Verifications() {{
            queueContext.getService();
            times = 0;
        }};
    }

    @Test
    public void matchesSubmissionWithNoServiceAndDifferentAction(@Injectable QueueContext queueContext) {
        String action = "actionValue";

        new Expectations() {{
            queueContext.getAction();
            result = action;
        }};

        Assert.assertFalse(backendJMSQueueService.matchesQueueContext(null, "differentAction", queueContext));

        new Verifications() {{
            queueContext.getService();
            times = 0;
        }};
    }

    @Test
    public void matchesSubmissionWithNoServiceAndNoAction(@Injectable QueueContext queueContext) {
        Assert.assertFalse(backendJMSQueueService.matchesQueueContext(null, null, queueContext));
    }
}
