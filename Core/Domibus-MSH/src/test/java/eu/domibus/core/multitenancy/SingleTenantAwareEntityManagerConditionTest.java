package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.jpa.SingleTenantAwareEntityManagerCondition;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class SingleTenantAwareEntityManagerConditionTest {

    @Tested
    private SingleTenantAwareEntityManagerCondition singleTenantAwareEntityManagerCondition;

    @Injectable
    private ConditionContext context;

    @Injectable
    private Environment environment;

    @Test
    public void testMatches_falseWhenNoEnvironment() {
        new Expectations() {{
           context.getEnvironment(); result = null;
        }};

        boolean matches = singleTenantAwareEntityManagerCondition.matches(context, null);

        assertFalse("Should have not matched when the environment is null", matches);
    }

    @Test
    public void testMatches_trueWhenEnvironmentDoesNotContainTheMultitenancyProperty() {
        new Expectations() {{
           context.getEnvironment(); result = environment;
           environment.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY); result = null;
        }};

        boolean matches = singleTenantAwareEntityManagerCondition.matches(context, null);

        assertTrue("Should have matched when the environment doesn't contain the multitenancy property", matches);
    }

    @Test
    public void testMatches_trueWhenEnvironmentContainsTheMultitenancyPropertySetToFalse() {
        new Expectations() {{
           context.getEnvironment(); result = environment;
           environment.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY); result = "general_schema";
        }};

        boolean matches = singleTenantAwareEntityManagerCondition.matches(context, null);

        assertFalse("Should have not matched when the environment contains the multitenancy property", matches);
    }
}