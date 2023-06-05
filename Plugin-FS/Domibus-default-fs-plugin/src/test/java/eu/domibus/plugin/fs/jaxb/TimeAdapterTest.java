package eu.domibus.plugin.fs.jaxb;

import eu.domibus.plugin.convert.StringToTemporalAccessorConverter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class TimeAdapterTest {

    @Injectable
    private StringToTemporalAccessorConverter converter;

    @Tested(availableDuringSetup = true)
    private TimeAdapter timeAdapter;

    @Before
    public void setup() throws IllegalAccessException {
        FieldUtils.writeField(timeAdapter, "converter", converter, true);
    }

    @Test
    public void testUnmarshall_returnsNullTimeForNullInputString() throws Exception {
        // GIVEN
        String input = null;
        new Expectations() {{
            converter.convert(input); result = null;
        }};

        // WHEN
        LocalTime result = timeAdapter.unmarshal(input);

        // THEN
        Assert.assertNull("Should have returned null when unmarshalling a null input string", result);
    }

    @Test
    public void testUnmarshall_returnsParsedTimeForNonNullInputString(@Injectable LocalTime parsedTime) throws Exception {
        // GIVEN
        String input = "09:34:36";
        new Expectations() {{
            converter.convert(input); result = parsedTime;
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
    public void testMarshall_returnsFormattedTimeForNonNullInputTime(@Injectable LocalTime input) throws Exception {
        // GIVEN
        String formattedTime = "09:34:36";
        new Expectations() {{
            input.format(DateTimeFormatter.ISO_TIME);
            result = formattedTime;
        }};

        // WHEN
        String result = timeAdapter.marshal(input);

        // THEN
        Assert.assertEquals("Should have returned the formatted time when marshalling a non-null input time", formattedTime, result);
    }
}
