package eu.domibus.common.model.configuration;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static eu.domibus.core.ebms3.sender.retry.RetryStrategy.CONSTANT;
import static eu.domibus.core.ebms3.sender.retry.RetryStrategy.SEND_ONCE;

/**
 * @author François Gautier
 * @since 4.2.0
 */
public class ReceptionAwarenessTest {

    private ReceptionAwareness receptionAwareness;

    @Before
    public void setUp() {
        receptionAwareness = new ReceptionAwareness();
    }

    @Test
    public void init_null() {
        receptionAwareness.init(null);

        Assert.assertEquals(SEND_ONCE, receptionAwareness.getStrategy());
        Assert.assertEquals(0, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(0, receptionAwareness.getRetryCount());
    }

    @Test
    public void init_okEmpty() {
        receptionAwareness.init(null);

        Assert.assertEquals(SEND_ONCE, receptionAwareness.getStrategy());
        Assert.assertEquals(0, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(0, receptionAwareness.getRetryCount());
    }

    @Test
    public void init_ok() {
        receptionAwareness.retryXml = "2;3;CONSTANT";
        receptionAwareness.init(null);

        Assert.assertEquals(CONSTANT, receptionAwareness.getStrategy());
        Assert.assertEquals(2, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(3, receptionAwareness.getRetryCount());
    }

    @Test
    public void init_ExceptionWrongStrategy() {
        receptionAwareness.retryXml = "2;3;TEST";
        try {
            receptionAwareness.init(null);
            Assert.fail();
        } catch (DomibusCoreException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.containsString(DomibusCoreErrorCode.DOM_003.getErrorCode()));
        }

        Assert.assertEquals(SEND_ONCE, receptionAwareness.getStrategy());
        Assert.assertEquals(2, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(3, receptionAwareness.getRetryCount());
    }

    @Test
    public void init_exception() {
        receptionAwareness.retryXml = "";

        try {
            receptionAwareness.init(null);
            Assert.fail();
        } catch (DomibusCoreException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.containsString(DomibusCoreErrorCode.DOM_003.getErrorCode()));
        }

        Assert.assertEquals(SEND_ONCE, receptionAwareness.getStrategy());
        Assert.assertEquals(0, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(0, receptionAwareness.getRetryCount());
    }

    @Test
    public void init_exception2() {
        receptionAwareness.retryXml = ";;";

        try {
            receptionAwareness.init(null);
            Assert.fail();
        } catch (DomibusCoreException e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.containsString(DomibusCoreErrorCode.DOM_003.getErrorCode()));
        }

        Assert.assertEquals(SEND_ONCE, receptionAwareness.getStrategy());
        Assert.assertEquals(0, receptionAwareness.getRetryTimeout());
        Assert.assertEquals(0, receptionAwareness.getRetryCount());
    }
}