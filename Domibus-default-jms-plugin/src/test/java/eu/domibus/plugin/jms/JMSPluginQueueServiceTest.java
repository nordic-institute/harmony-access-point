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
public class JMSPluginQueueServiceTest {

    @Tested
    JMSPluginQueueService jmsPluginQueueService;

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
        long messageEntityId = 123;
        String service = "myService";
        String action = "myAction";
        String defaultQueueProperty = "domibus.defaultQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";

        new Expectations(jmsPluginQueueService) {{
            jmsPluginQueueService.getJMSQueue((QueueContext) any, anyString, anyString);
        }};

        final QueueContext queueContext = new QueueContext(messageId, service, action);
        jmsPluginQueueService.getJMSQueue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);

        new Verifications() {{
            QueueContext queueContext = null;
            jmsPluginQueueService.getJMSQueue(queueContext = withCapture(), defaultQueueProperty, routingQueuePrefixProperty);

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

        new Expectations(jmsPluginQueueService) {{
            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            domibusPropertyExtService.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            jmsPluginQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
            result = routingQueueValue;
        }};

        String queueValue = jmsPluginQueueService.getQueueValue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
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

        new Expectations(jmsPluginQueueService) {{
            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            domibusPropertyExtService.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            jmsPluginQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
            result = null;

            domibusPropertyExtService.getProperty(domainDTO, defaultQueueProperty);
            result = defaultQueueValue;
        }};

        String queueValue = jmsPluginQueueService.getQueueValue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
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

        new Expectations(jmsPluginQueueService) {{
            jmsPluginQueueService.getRoutingQueue(routingQueuePrefixProperty, prefix, queueContext, domainDTO);
            result = queueValue;
        }};

        String routingQueueValue = jmsPluginQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, domainDTO);
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

        new Expectations(jmsPluginQueueService) {{
            jmsPluginQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "service");
            result = serviceProperty;

            domibusPropertyExtService.getProperty(domainDTO, serviceProperty);
            result = serviceValue;

            jmsPluginQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "action");
            result = actionProperty;

            domibusPropertyExtService.getProperty(domainDTO, actionProperty);
            result = actionValue;

            jmsPluginQueueService.matchesQueueContext(serviceValue, actionValue, queueContext);
            result = true;

            jmsPluginQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "queue");
            result = queueProperty;

            domibusPropertyExtService.getProperty(domainDTO, queueProperty);
            result = queueValue;
        }};

        String routingQueue = jmsPluginQueueService.getRoutingQueue(routingQueuePrefixProperty, routingQueuePrefixName, queueContext, domainDTO);
        Assert.assertEquals(routingQueue, queueValue);
    }

    @Test
    public void getQueuePropertyName() {
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing";
        String routingQueuePrefixName = "rule1";

        String suffix = "queue";
        String queue = jmsPluginQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, suffix);
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

        Assert.assertTrue(jmsPluginQueueService.matchesQueueContext(service, action, queueContext));
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

        Assert.assertFalse(jmsPluginQueueService.matchesQueueContext(service, "myAction", queueContext));
    }

    @Test
    public void matchesSubmissionWithServiceAndNoAction(@Injectable QueueContext queueContext) {
        String service = "serviceValue";

        new Expectations() {{
            queueContext.getService();
            result = service;
        }};

        Assert.assertTrue(jmsPluginQueueService.matchesQueueContext(service, null, queueContext));

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

        Assert.assertFalse(jmsPluginQueueService.matchesQueueContext("differentService", null, queueContext));

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

        Assert.assertTrue(jmsPluginQueueService.matchesQueueContext(null, action, queueContext));

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

        Assert.assertFalse(jmsPluginQueueService.matchesQueueContext(null, "differentAction", queueContext));

        new Verifications() {{
            queueContext.getService();
            times = 0;
        }};
    }

    @Test
    public void matchesSubmissionWithNoServiceAndNoAction(@Injectable QueueContext queueContext) {
        Assert.assertFalse(jmsPluginQueueService.matchesQueueContext(null, null, queueContext));
    }
}
