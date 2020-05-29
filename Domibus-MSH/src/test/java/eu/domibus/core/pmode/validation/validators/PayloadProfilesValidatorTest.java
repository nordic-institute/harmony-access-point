package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Payload;
import eu.domibus.common.model.configuration.PayloadProfile;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @since 4.2
 * @author Catalin Enache
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
                              final @Mocked Payload payload,
                              final @Mocked List<ValidationIssue> issues) {

        final Set<Payload> validPayloads = Collections.singleton(payload);
        final Set<PayloadProfile> payloadProfileList = Collections.singleton(payloadProfile);

        new Expectations(payloadProfilesValidator) {{
//            new ArrayList<>();
//            result = issues;

            configuration.getBusinessProcesses().getPayloadProfiles();
            result = payloadProfileList;

            configuration.getBusinessProcesses().getPayloads();
            result = validPayloads;
        }};


        payloadProfilesValidator.validate(configuration);

        new FullVerifications() {{
            payloadProfileList.forEach((Consumer) any);

            payloadProfilesValidator.validatePayloadProfile(payloadProfile, validPayloads, issues);
        }};
    }

    @Test
    public void test_validatePayloadProfile() {
    }

    @Test
    public void test_createIssue() {
    }
}