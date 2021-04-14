package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.test.util.MessageTestUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public class Ebms3ConverterTestIT extends AbstractIT {

    @Autowired
    Ebms3Converter ebms3Converter;

    @Test
    public void testEbms3ConversionToEntityAndViceVersa() {
        final UserMessage userMessage = new MessageTestUtility().createSampleUserMessage();
        Ebms3UserMessage ebms3UserMessage = ebms3Converter.convertToEbms3(userMessage);

        UserMessage converted = ebms3Converter.convertFromEbms3(ebms3UserMessage);
        assertEquals(userMessage, converted);

    }
}
