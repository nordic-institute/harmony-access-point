package eu.domibus.common.model.configuration;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.pmode.PModeValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@RunWith(Parameterized.class)
public class ReceptionAwarenessProgressiveTest {

    @Parameterized.Parameter(0)
    public String progressiveConfig; // as (timeout, initialInterval, multiplyingFactor)
    @Parameterized.Parameter(1)
    public List expectedProgressiveIntervals;
    @Parameterized.Parameter(2)
    public Class<? extends Exception> validationException;

    private ReceptionAwareness receptionAwareness;

    private static final List NO_RETRY = Arrays.asList();
    private static final Exception NO_VALIDATION_EXCEPTION = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        receptionAwareness = new ReceptionAwareness();
    }

    @Parameterized.Parameters(name = "case {index}: ({0})")
    public static Collection input() {
        List data = new ArrayList();
        data.add(new Object[]{"9;1;2;", Arrays.asList(1,2,4,8), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"100;1;3;", Arrays.asList(1,3,9,27,81), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"100;2;3;", Arrays.asList(2,6,18,54), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"100;20;3;", Arrays.asList(20,60), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"100;3;2;", Arrays.asList(3,6,12,24,48,96), NO_VALIDATION_EXCEPTION});
        //extreme cases:
        data.add(new Object[]{"100000;1;2;", Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"10000000;1;10;", Arrays.asList(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000), NO_VALIDATION_EXCEPTION});

        // edge cases:
        data.add(new Object[]{"8;1;2;", Arrays.asList(1,2,4,8), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"2;1;2;", Arrays.asList(1, 2), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"1;1;2;", Arrays.asList(1), NO_VALIDATION_EXCEPTION});
        data.add(new Object[]{"1;2;2;", NO_RETRY, PModeValidationException.class});
        data.add(new Object[]{"10;0;2;", NO_RETRY, PModeValidationException.class});
        data.add(new Object[]{"10;1;0;", NO_RETRY, PModeValidationException.class});

        //validation errors
        data.add(new Object[]{"100;400;3;", NO_RETRY, PModeValidationException.class});
        data.add(new Object[]{"10;11;3;", Arrays.asList(), PModeValidationException.class});
        data.add(new Object[]{"1;10;1;", NO_RETRY, PModeValidationException.class});
        data.add(new Object[]{"1;1;1;", NO_RETRY, PModeValidationException.class});
        data.add(new Object[]{"1.5;1;1;", NO_RETRY, DomibusCoreException.class});
        data.add(new Object[]{"-2;-3;1;", NO_RETRY, DomibusCoreException.class});

        return data;
        }

    @Test
    public void calculateRetryIntervals_for_progressive() {
        if (validationException!=null) {
            thrown.expect(validationException);
        }
        receptionAwareness.retryXml = progressiveConfig + "PROGRESSIVE";
        receptionAwareness.init(null);

        System.out.printf("Progressive retry intervals for (%d, %d, %d) are: " + expectedProgressiveIntervals.toString(),
                receptionAwareness.getInitialInterval(), receptionAwareness.getMultiplyingFactor(),
                receptionAwareness.getRetryTimeout());
        System.out.println();
        assertEquals(expectedProgressiveIntervals, receptionAwareness.getRetryIntervals());
    }

}
