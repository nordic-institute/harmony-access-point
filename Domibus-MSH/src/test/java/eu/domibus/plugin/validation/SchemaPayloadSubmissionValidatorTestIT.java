package eu.domibus.plugin.validation;

import eu.domibus.plugin.Submission;
import mockit.Expectations;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class SchemaPayloadSubmissionValidatorTestIT {

    SchemaPayloadSubmissionValidator schemaPayloadSubmissionValidator;

    @Before
    public void init() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.core.plugin.validation");
        schemaPayloadSubmissionValidator = new SchemaPayloadSubmissionValidator();
        schemaPayloadSubmissionValidator.setJaxbContext(jaxbContext);
        schemaPayloadSubmissionValidator.setSchema(new ClassPathResource("eu/domibus/core/plugin/validation/payload.xsd"));
    }

    @Test
    public void testValidatePayloadWithValidPayload(@Injectable final Submission.Payload payload) throws Exception {
        new Expectations() {{
            payload.getPayloadDatahandler().getInputStream();
            returns(new ClassPathResource("eu/domibus/core/plugin/validation/validPayload.xml").getInputStream());
        }};

        schemaPayloadSubmissionValidator.validatePayload(payload);
    }

    @Test(expected = SubmissionValidationException.class)
    public void testValidatePayloadWithValidInvalidPayload(@Injectable final Submission.Payload payload) throws Exception {
        new Expectations() {{
            payload.getPayloadDatahandler().getInputStream();
            returns(new ClassPathResource("eu/domibus/core/plugin/validation/invalidPayload.xml").getInputStream());
        }};

        schemaPayloadSubmissionValidator.validatePayload(payload);
    }
}
