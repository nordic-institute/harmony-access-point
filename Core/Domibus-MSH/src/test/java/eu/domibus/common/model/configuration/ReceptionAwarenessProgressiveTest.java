package eu.domibus.common.model.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Razvan Cretu
 * @since 5.1.0
 */
@RunWith(Parameterized.class)
public class ReceptionAwarenessProgressiveTest {

    private int initialInterval;
    private int multiplyingFactor;
    private int timeout;
    private List expectedProgressiveIntervals;

    public ReceptionAwarenessProgressiveTest(int initialInterval, int multiplyingFactor, int timeout, List expectedProgressiveIntervals) {
        super();
        this.initialInterval = initialInterval;
        this.multiplyingFactor = multiplyingFactor;
        this.timeout = timeout;
        this.expectedProgressiveIntervals = expectedProgressiveIntervals;
    }

    private ReceptionAwareness receptionAwareness;

    @Before
    public void setUp() {
        receptionAwareness = new ReceptionAwareness();
    }

    @Parameterized.Parameters
    public static Collection input() {
        return Arrays.asList(new Object[][]{
                {1, 2, 9, Arrays.asList(1,2,4,8)},
                {1, 3, 100, Arrays.asList(1,3,9,27,81)},
                {2, 3, 100, Arrays.asList(2,6,18,54)},
                {20, 3, 100, Arrays.asList(20,60)},
                {3, 2, 100, Arrays.asList(3,6,12,24,48,96)}
        })
    ;}

    @Test
    public void calculateRetryIntervals_for_progressive() {
        List result = receptionAwareness.calculateRetryIntervals(initialInterval, multiplyingFactor, timeout);
        System.out.printf("Progressive retry intervals for (%d, %d, %d) are: " + expectedProgressiveIntervals.toString(), initialInterval, multiplyingFactor, timeout);
        System.out.println();
        assertEquals(expectedProgressiveIntervals, result);
    }

}
