package eu.domibus.core.ebms3.mapper;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.receiver.MessageTestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Ebms3ConverterTestIT.ContextConfiguration.class})
@DirtiesContext
@Rollback
public class Ebms3ConverterTestIT {

    @ComponentScan(basePackages = "eu.domibus.core.ebms3.mapper")
    @Configuration
    static class ContextConfiguration {
    }

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
