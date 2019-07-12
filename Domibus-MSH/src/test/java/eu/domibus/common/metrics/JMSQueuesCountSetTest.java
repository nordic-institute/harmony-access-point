package eu.domibus.common.metrics;

import com.codahale.metrics.Metric;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

/**
 * @since 4.1.1
 * @author Catalin Enache *
 */
public class JMSQueuesCountSetTest {

    @Tested
    JMSQueuesCountSet jmsQueuesCountSet;

    @Injectable
    JMSManager jmsManager;

    @Test
    public void test_GetMetrics() {
        final Map<String, JMSDestination> jmsDestinationList = new TreeMap<>();
        final long nbMessages = 20;
        final String queueName = "domibus.DLQ";
        final JMSDestination jmsDestination = new JMSDestination();
        jmsDestination.setName(queueName);
        jmsDestination.setNumberOfMessages(nbMessages);
        jmsDestination.setType("Queue");
        jmsDestination.setInternal(true);
        jmsDestinationList.put(queueName, jmsDestination);

        new Expectations() {{
            jmsManager.getDestinations();
            result = jmsDestinationList;
        }};

        //tested method
        final Map<String, Metric> metrics = jmsQueuesCountSet.getMetrics();

        Assert.assertNotNull(metrics);
        Assert.assertTrue(metrics.size() == 1);
        Assert.assertTrue(metrics.containsKey(queueName));
    }
}