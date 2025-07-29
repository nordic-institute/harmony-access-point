package eu.domibus.logging;

import eu.domibus.logging.api.MessageCode;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DefaultMessageConverterTest {

    private DefaultMessageConverter defaultMessageConverter;

    @Before
    public void setUp() throws Exception {
        defaultMessageConverter = new DefaultMessageConverter();
    }

    @Test
    public void testGetMessageWithMarker() throws Exception {
        final MessageCode testMessageCode = new MessageCode() {
            @Override
            public String getCode() {
                return "myTestCode";
            }

            @Override
            public String getMessage() {
                return "test message {}";
            }
        };
        final String message = defaultMessageConverter.getMessage(DomibusLogger.BUSINESS_MARKER, testMessageCode, "param1");
        assertEquals("[BUSINESS - myTestCode] test message param1", message);
    }

    @Test
    public void testGetMessage() throws Exception {
        final MessageCode testMessageCode = new MessageCode() {
            @Override
            public String getCode() {
                return "myTestCode";
            }

            @Override
            public String getMessage() {
                return "test message {}";
            }
        };
        final String message = defaultMessageConverter.getMessage(null, testMessageCode, "param1");
        assertEquals("[myTestCode] test message param1", message);
    }


}
