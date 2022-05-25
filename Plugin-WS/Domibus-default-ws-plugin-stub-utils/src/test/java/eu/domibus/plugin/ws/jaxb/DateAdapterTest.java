package eu.domibus.plugin.ws.jaxb;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DateAdapterTest {

    @Injectable
    private StringToTemporalAccessorConverter converter;

    @Tested(availableDuringSetup = true)
    private DateAdapter dateAdapter;

    @Before
    public void setup() throws IllegalAccessException {
        FieldUtils.writeField(dateAdapter, "converter", converter, true);
    }

    @Test
    public void testUnmarshall_returnsNullDateForNullInputString() throws Exception {
        // GIVEN
        String input = null;
        new Expectations() {{
            converter.convert(input); result = null;
        }};

        // WHEN
        LocalDate result = dateAdapter.unmarshal(input);

        // THEN
        Assert.assertNull("Should have returned null when unmarshalling a null input string", result);
    }

    @Test
    public void testUnmarshall_returnsParsedDateForNonNullInputString(@Injectable LocalDate parsedDate) throws Exception {
        // GIVEN
        String input = "2019-04-17";
        new Expectations() {{
            converter.convert(input); result = parsedDate;
        }};

        // WHEN
        LocalDate result = dateAdapter.unmarshal(input);

        // THEN
        Assert.assertSame("Should have returned the parsed date when unmarshalling a non-null input string", parsedDate, result);
    }

    @Test
    public void testMarshal_returnsNullFormattedDateForNullInputDate() throws Exception {
        // GIVEN
        LocalDate input = null;

        // WHEN
        String result = dateAdapter.marshal(input);

        // THEN
        Assert.assertNull("Should have returned null when marshalling a null input date", result);
    }


    @Test
    public void testMarshall_returnsFormattedDateForNonNullInputDate(@Injectable LocalDate inputDate) throws Exception {
        // GIVEN
        String formattedDate = "2019-04-17";
        new Expectations() {{
            inputDate.format(DateTimeFormatter.ISO_DATE); result = formattedDate;
        }};

        // WHEN
        String result = dateAdapter.marshal(inputDate);

        // THEN
        Assert.assertEquals("Should have returned the formatted date when marshalling a non-null input date", formattedDate, result);
    }
}