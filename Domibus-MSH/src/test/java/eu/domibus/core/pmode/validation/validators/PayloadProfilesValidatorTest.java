package eu.domibus.core.pmode.validation.validators;

import eu.domibus.common.model.configuration.Attachment;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PayloadProfilesValidatorTest {

    @Tested
    PayloadProfilesValidator payloadProfilesValidator;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Test
    public void test_validate(final @Mocked Configuration configuration,
                              final @Mocked PayloadProfile payloadProfile,
                              final @Mocked Set<Payload> validPayloads) {


        final Set<PayloadProfile> payloadProfileList = Collections.singleton(payloadProfile);

        new Expectations() {{
            configuration.getBusinessProcesses().getPayloadProfiles();
            result = payloadProfileList;

            configuration.getBusinessProcesses().getPayloads();
            result = validPayloads;
        }};


        payloadProfilesValidator.validate(configuration);

        new FullVerifications() {{
            payloadProfilesValidator.validatePayloadProfile(payloadProfile, validPayloads);
        }};
    }

    @Test
    public void test_validatePayloadProfile(final @Mocked PayloadProfile payloadProfile,
                                            final @Mocked Set<Payload> validPayloads) {
        final List<Attachment> attachmentList = new ArrayList<>();

        new Expectations() {{
            pModeValidationHelper.getAttributeValue(payloadProfile, "attachment", List.class);
            result = attachmentList;

            payloadProfile.getMaxSize();
            result = 400;
        }};

        payloadProfilesValidator.validatePayloadProfile(payloadProfile, validPayloads);

        new FullVerifications() {{
        }};
    }

    @Test
    public void test_validatePayloadProfile_MaxSizeNegative(final @Mocked PayloadProfile payloadProfile,
                                                            final @Mocked Set<Payload> validPayloads,
                                                            final @Mocked List<Attachment> attachmentList) {
        new Expectations(payloadProfilesValidator) {{
            pModeValidationHelper.getAttributeValue(payloadProfile, "attachment", List.class);
            result = attachmentList;

            payloadProfile.getMaxSize();
            result = -20;
        }};

        payloadProfilesValidator.validatePayloadProfile(payloadProfile, validPayloads);

        new FullVerifications(payloadProfilesValidator) {{
            payloadProfilesValidator.createIssue(payloadProfile, anyString, anyString);
        }};
    }

    @Test
    public void test_createIssue(final @Mocked PayloadProfile payloadProfile) {
        final String message = "message";
        final String name = "name";

        //tested method
        payloadProfilesValidator.createIssue(payloadProfile, name, message);

        new FullVerifications() {{
            pModeValidationHelper.createValidationIssue(message, name, payloadProfile.getName());
        }};
    }
}