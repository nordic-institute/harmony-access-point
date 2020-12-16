package eu.domibus.plugin.webservice.jaxb;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DateTimeAdapterTest {

    @Tested
    private DateTimeAdapter dateTimeAdapter;

    @Test
    public void testUnmarshall_returnsNullDateTimeForNullInputString() throws Exception {
        // GIVEN
        String input = null;

        // WHEN
        LocalDateTime result = dateTimeAdapter.unmarshal(input);

        // THEN
        Assert.assertNull("Should have returned null when unmarshalling a null input string", result);
    }

    @Test
    public void testUnmarshall_returnsParsedDateTimeForNonNullInputString(@Injectable LocalDateTime parsedDateTime) throws Exception {
        // GIVEN
        String input = "any";
        new Expectations(LocalDateTime.class) {{
            LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME); result = parsedDateTime;
        }};

        // WHEN
        LocalDateTime result = dateTimeAdapter.unmarshal(input);

        // THEN
        Assert.assertSame("Should have returned the parsed date time when unmarshalling a non-null input string", parsedDateTime, result);
    }

    @Test
    public void testMarshal_returnsNullFormattedDateTimeForNullInputDateTime() throws Exception {
        // GIVEN
        LocalDateTime input = null;

        // WHEN
        String result = dateTimeAdapter.marshal(input);

        // THEN
        Assert.assertNull("Should have returned null when marshalling a null input date time", result);
    }


    @Test
    public void testUnmarshall_returnsFormattedDateTimeForNonNullInputDateTime(@Injectable LocalDateTime inputDate) throws Exception {
        // GIVEN
        String formattedDateTime = "2019-04-17T09:34:36";
        new Expectations() {{
            inputDate.format(DateTimeFormatter.ISO_DATE_TIME); result = formattedDateTime;
        }};

        // WHEN
        String result = dateTimeAdapter.marshal(inputDate);

        // THEN
        Assert.assertEquals("Should have returned the formatted date time when marshalling a non-null input date time", formattedDateTime, result);
    }
}