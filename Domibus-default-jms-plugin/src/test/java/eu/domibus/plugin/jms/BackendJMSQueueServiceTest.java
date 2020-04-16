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
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void getJMSQueue() {
        String messageId = "123";
        String defaultQueueProperty = "domibus.defaultQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";

        new Expectations(backendJMSQueueService) {{
            backendJMSQueueService.getQueueValue(messageId, defaultQueueProperty, routingQueuePrefixProperty);
            result = defaultQueueProperty;
        }};

        String jmsQueue = backendJMSQueueService.getJMSQueue(messageId, defaultQueueProperty, routingQueuePrefixProperty);
        assertEquals(defaultQueueProperty, jmsQueue);
    }

    @Test
    public void getQueueValueWithRouting(@Injectable Submission submission,
                                         @Injectable DomainDTO domainDTO) throws MessageNotFoundException {
        String messageId = "123";
        String defaultQueueProperty = "domibus.defaultQueue";
        String routingQueueValue = "domibus.myQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";


        List<String> routingQueuePrefixNameList = new ArrayList<>();
        routingQueuePrefixNameList.add("routing.rule1");

        new Expectations(backendJMSQueueService) {{
            messageRetriever.browseMessage(messageId);
            result = submission;

            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            backendJMSQueueService.getQueuePrefix(domainDTO, routingQueuePrefixProperty);
            result = routingQueuePrefixProperty;

            jmsPluginPropertyManager.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, submission, domainDTO);
            result = routingQueueValue;
        }};

        String queueValue = backendJMSQueueService.getQueueValue(messageId, defaultQueueProperty, routingQueuePrefixProperty);
        Assert.assertEquals(queueValue, routingQueueValue);
    }

    @Test
    public void getQueueValueWithDefaultQueue(@Injectable Submission submission,
                                              @Injectable DomainDTO domainDTO) throws MessageNotFoundException {
        String messageId = "123";
        String defaultQueueProperty = "domibus.defaultQueue";
        String defaultQueueValue = "domibus.myQueue";
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";


        List<String> routingQueuePrefixNameList = new ArrayList<>();
        routingQueuePrefixNameList.add("routing.rule1");

        new Expectations(backendJMSQueueService) {{
            messageRetriever.browseMessage(messageId);
            result = submission;

            domainContextExtService.getCurrentDomain();
            result = domainDTO;

            backendJMSQueueService.getQueuePrefix(domainDTO, routingQueuePrefixProperty);
            result = routingQueuePrefixProperty;

            jmsPluginPropertyManager.getNestedProperties(routingQueuePrefixProperty);
            result = routingQueuePrefixNameList;

            backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, submission, domainDTO);
            result = null;

            domibusPropertyExtService.getProperty(domainDTO, defaultQueueProperty);
            result = defaultQueueValue;
        }};

        String queueValue = backendJMSQueueService.getQueueValue(messageId, defaultQueueProperty, routingQueuePrefixProperty);
        Assert.assertEquals(queueValue, defaultQueueValue);
    }

    @Test
    public void getRoutingQueueValue(@Injectable Submission submission,
                                     @Injectable DomainDTO domainDTO) {
        List<String> routingQueuePrefixNameList = new ArrayList<>();
        String prefix = "routing.rule1";
        routingQueuePrefixNameList.add(prefix);
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";
        String queueValue = "domibus.myQueue";

        new Expectations(backendJMSQueueService) {{
            backendJMSQueueService.getRoutingQueue(routingQueuePrefixProperty, prefix, submission, domainDTO);
            result = queueValue;
        }};

        String routingQueueValue = backendJMSQueueService.getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, submission, domainDTO);
        Assert.assertEquals(queueValue, routingQueueValue);
    }

    @Test
    public void getRoutingQueue(@Injectable Submission submission,
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

            backendJMSQueueService.matchesSubmission(serviceValue, actionValue, submission);
            result = true;

            backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "queue");
            result = queueProperty;

            domibusPropertyExtService.getProperty(domainDTO, queueProperty);
            result = queueValue;
        }};

        String routingQueue = backendJMSQueueService.getRoutingQueue(routingQueuePrefixProperty, routingQueuePrefixName, submission, domainDTO);
        Assert.assertEquals(routingQueue, queueValue);
    }

    @Test
    public void getQueuePropertyName() {
        String routingQueuePrefixProperty = "domibus.defaultQueue.routing.";
        String routingQueuePrefixName = "rule1";

        String suffix = "queue";
        String queue = backendJMSQueueService.getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, suffix);
        Assert.assertEquals(routingQueuePrefixProperty + routingQueuePrefixName + "." + suffix, queue);
    }

    @Test
    public void getQueuePrefixForDefaultDomain() {
        String value = backendJMSQueueService.getQueuePrefix(DomainDTO.DEFAULT_DOMAIN, "rule1");
        Assert.assertEquals("rule1", value);
    }

    @Test
    public void getQueuePrefix(@Injectable DomainDTO domainDTO) {
        String domainCode = "digit";

        new Expectations() {{
            domainDTO.getCode();
            result = domainCode;
        }};

        String value = backendJMSQueueService.getQueuePrefix(domainDTO, "rule1");
        Assert.assertEquals(domainCode + ".rule1", value);
    }

    @Test
    public void matchesSubmission(@Injectable Submission submission) {
        String service = "serviceValue";
        String action = "actionValue";

        new Expectations() {{
            submission.getService();
            result = service;

            submission.getAction();
            result = action;
        }};

        Assert.assertTrue(backendJMSQueueService.matchesSubmission(service, action, submission));
    }
}
