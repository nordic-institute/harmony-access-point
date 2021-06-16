package eu.domibus.plugin.ws.jaxb;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class TimeAdapterTest {

    @Tested
    private TimeAdapter timeAdapter;

    @Test
    public void testUnmarshall_returnsNullTimeForNullInputString() throws Exception {
        // GIVEN
        String input = null;

        // WHEN
        LocalTime result = timeAdapter.unmarshal(input);

        // THEN
        Assert.assertNull("Should have returned null when unmarshalling a null input string", result);
    }

    @Test
    public void testUnmarshall_returnsParsedTimeForNonNullInputString(@Injectable LocalTime parsedTime) throws Exception {
        // GIVEN
        String input = "any";
        new Expectations(LocalTime.class) {{
            LocalTime.parse(input, DateTimeFormatter.ISO_TIME); result = parsedTime;
        }};

        // WHEN
        LocalTime result = timeAdapter.unmarshal(input);

        // THEN
        Assert.assertSame("Should have returned the parsed time when unmarshalling a non-null input string", parsedTime, result);
    }

    @Test
    public void testMarshal_returnsNullFormattedTimeForNullInputTime() throws Exception {
        // GIVEN
        LocalTime input = null;

        // WHEN
        String result = timeAdapter.marshal(input);

        // THEN
        Assert.assertNull("Should have returned null when marshalling a null input time", result);
    }


    @Test
    public void testUnmarshall_returnsFormattedTimeForNonNullInputTime(@Injectable LocalTime input) throws Exception {
        // GIVEN
        String formattedTime = "09:34:36";
        new Expectations() {{
            input.format(DateTimeFormatter.ISO_TIME); result = formattedTime;
        }};

        // WHEN
        String result = timeAdapter.marshal(input);

        // THEN
        Assert.assertEquals("Should have returned the formatted time when marshalling a non-null input time", formattedTime, result);
    }
}