package eu.domibus.core.util;

import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class RegexUtilImplTest {

    @Tested
    RegexUtilImpl regexUtil;

    @Test
    public void testWildflyQueueNameIsInternal() {
        final String queueName = "jms.queue.DomibusAlertMessageQueue";
        final String INTERNALQUEUE_EXPRESSION = ".*jms.queue.(Domibus[a-zA-Z]|DLQ|ExpiryQueue|internal|backend.jms|notification.jms|notification.webservice|notification.kerkovi|notification.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        ;
        Assert.assertTrue(internal);
    }

    @Test
    public void testWildflyQueueNameIsNotInternal() {
        final String queueName = "jms.DomibusAlertMessageQueue";
        final String INTERNALQUEUE_EXPRESSION = ".*jms.queue.(Domibus[a-zA-Z]|DLQ|ExpiryQueue|internal|backend.jms|notification.jms|notification.webservice|notification.kerkovi|notification.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        Assert.assertFalse(internal);
    }

    @Test
    public void testWeblogicQueueNameIsInternal() {
        final String queueName = "domibus.backend.jms.inQueue ";
        final String INTERNALQUEUE_EXPRESSION = ".*domibus.(internal|DLQ|backend|.jms|notification|.jms|notification|.webservice|notification|.kerkovi|notification|.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        Assert.assertTrue(internal);
    }

    @Test
    public void testWeblogicQueueNameIsNotInternal() {
        final String queueName = "backend.jms.inQueue ";
        final String INTERNALQUEUE_EXPRESSION = ".*domibus.(internal|DLQ|backend|.jms|notification|.jms|notification|.webservice|notification|.kerkovi|notification|.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        Assert.assertFalse(internal);
    }

    @Test
    public void testTomcatQueueNameIsInternal() {
        final String queueName = "domibus.backend.jms.outQueue";
        final String INTERNALQUEUE_EXPRESSION = ".*domibus.(internal|DLQ|backend|.jms|notification|.jms|notification|.webservice|notification|.kerkovi|notification|.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        Assert.assertTrue(internal);
    }

    @Test
    public void testTomcatQueueNameIsNotInternal() {
        final String queueName = "jms.outQueue";
        final String INTERNALQUEUE_EXPRESSION = ".*domibus.(internal|DLQ|backend|.jms|notification|.jms|notification|.webservice|notification|.kerkovi|notification|.filesystem).*";

        boolean internal = regexUtil.matches(INTERNALQUEUE_EXPRESSION, queueName);
        Assert.assertFalse(internal);
    }

}