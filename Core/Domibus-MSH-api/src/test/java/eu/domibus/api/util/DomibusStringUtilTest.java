package eu.domibus.api.util;

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class DomibusStringUtilTest {

    @Test
    public void sanitizeFileName() {
        final String fileName = "/test-String&123@97.txt";
        String result = DomibusStringUtil.sanitizeFileName(fileName);
        assertEquals(result, "_test-String_123@97.txt");

    }
}