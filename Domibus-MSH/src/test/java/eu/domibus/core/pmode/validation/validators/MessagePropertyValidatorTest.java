package eu.domibus.core.pmode.validation.validators;

import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.MessageProperties;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.Property;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.UUID;

/**
 * @author Catalin Enache
 * @since 4.1.5
 */
@RunWith(JMockit.class)
public class MessagePropertyValidatorTest {

    @Tested
    MessagePropertyValidator messagePropertyValidator;

    @Test
    public void test_validate_MessageProperty_OK(final @Mocked Messaging messaging,
                                                 final @Mocked MessageProperties messageProperties,
                                                 final @Mocked Property property) throws Exception {

        new Expectations() {{
            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "message " + UUID.randomUUID();

            messaging.getUserMessage().getMessageProperties();
            result = messageProperties;

            messaging.getUserMessage().getMessageProperties().getProperty();
            result = Collections.singleton(property);

            property.getValue();
            result = "test";

        }};

        messagePropertyValidator.validate(messaging, MSHRole.SENDING);

        new FullVerifications() {{

        }};
    }

    @Test
    public void test_validate_MessageProperty_Exception(final @Mocked Messaging messaging,
                                                        final @Mocked MessageProperties messageProperties,
                                                        final @Mocked Property property) throws Exception {

        new Expectations() {{
            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "message " + UUID.randomUUID();

            messaging.getUserMessage().getMessageProperties();
            result = messageProperties;

            messaging.getUserMessage().getMessageProperties().getProperty();
            result = Collections.singleton(property);

            property.getName();
            result = "errorMsg";

            property.getValue();
            result = "An error was received from eTranslation service while processing the message id: 5f663af9-39ef-450b-aa3d-b7a33624368c@domibus.eu. The message says: Translation request must be text type, document path type or document base64 type and not several at a time.An error was received from eTranslation service while processing the message id: 5f663af9-39ef-450b-aa3d-b7a33624368c@domibus.eu. The message says: Translation request must be text type, document path type or document base64 type and not several at a time.An error was received from eTranslation service while processing the message id: 5f663af9-39ef-450b-aa3d-b7a33624368c@domibus.eu. The message says: Translation request must be text type, document path type or document base64 type and not several at a time. An error was received from eTranslation service while processing the message id: 5f663af9-39ef-450b-aa3d-b7a33624368c@domibus.eu. The message says: Translation request must be text type, document path type or document base64 type and not several at a time.";

        }};

        try {
            messagePropertyValidator.validate(messaging, MSHRole.SENDING);
            Assert.fail("exception expected");
        } catch (EbMS3Exception e) {
            Assert.assertTrue(e.getMessage().contains("property has a value which exceeds 1024 characters size."));
        }
    }

    @Test
    public void test_validate_MessageProperty_Null(final @Mocked Messaging messaging,
                                                 final @Mocked MessageProperties messageProperties,
                                                 final @Mocked Property property) throws Exception {

        new Expectations() {{
            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "message " + UUID.randomUUID();

            messaging.getUserMessage().getMessageProperties();
            result = messageProperties;

            messaging.getUserMessage().getMessageProperties().getProperty();
            result = Collections.singleton(property);

            property.getValue();
            result = null;

        }};

        messagePropertyValidator.validate(messaging, MSHRole.SENDING);

        new FullVerifications() {{

        }};
    }
}