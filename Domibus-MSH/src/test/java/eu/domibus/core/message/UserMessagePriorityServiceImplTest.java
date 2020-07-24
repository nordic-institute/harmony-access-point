package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class UserMessagePriorityServiceImplTest {

    @Tested
    UserMessagePriorityServiceImpl userMessagePriorityService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    private static final String USER_MESSAGE_SERVICE = "my service";
    private static final String USER_MESSAGE_ACTION = "my action";

    @Test
    public void getPriority() {
        String rule1 = "rule1";
        String rule2 = "rule2";
        String priorityValueString = "3";
        Integer priorityValue = Integer.valueOf(priorityValueString);

        List<String> priorityRuleNames = Arrays.asList(rule1, rule2);

        new Expectations(userMessagePriorityService) {{
            domibusPropertyProvider.getNestedProperties(DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY);
            result = priorityRuleNames;

            userMessagePriorityService.getPriorityValue(priorityRuleNames, USER_MESSAGE_SERVICE, USER_MESSAGE_ACTION);
            result = priorityValueString;

            userMessagePriorityService.convertPriorityToInteger(priorityValueString);
            result = priorityValue;
        }};

        userMessagePriorityService.getPriority(USER_MESSAGE_SERVICE, USER_MESSAGE_ACTION);

        new Verifications() {{
            userMessagePriorityService.validatePriority(priorityValue);
        }};
    }

    @Test
    public void convertPriorityToInteger() {
        String priorityValueString = "3";
        Integer priorityValue = Integer.valueOf(priorityValueString);

        assertEquals(priorityValue, userMessagePriorityService.convertPriorityToInteger(priorityValueString));
    }

    @Test(expected = UserMessageException.class)
    public void validatePriorityWithHigherUnacceptedValue() {
        userMessagePriorityService.validatePriority(10);
    }

    @Test(expected = UserMessageException.class)
    public void validatePriorityWithLowerUnacceptedValue() {
        userMessagePriorityService.validatePriority(-1);
    }

    @Test
    public void validatePriority() {
        userMessagePriorityService.validatePriority(3);

        //no exception thrown
    }

    @Test
    public void getPriorityValue() {
        String rule1 = "rule1";
        String rule2 = "rule2";
        String priorityValueString = "3";

        List<String> priorityRuleNames = Arrays.asList(rule1, rule2);

        new Expectations(userMessagePriorityService) {{
            userMessagePriorityService.getMatchingPriority(rule2, USER_MESSAGE_SERVICE, USER_MESSAGE_SERVICE);
            result = priorityValueString;
        }};

        assertEquals(priorityValueString, userMessagePriorityService.getPriorityValue(priorityRuleNames, USER_MESSAGE_SERVICE, USER_MESSAGE_SERVICE));
    }

    @Test
    public void getMatchingPriority() {
        String rule1 = "rule1";
        String servicePropertyName = DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY + "." + "service";
        String actionPropertyName = DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY + "." + "action";
        String priorityPropertyName = DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY + "." + "value";

        String priorityValue = "3";

        new Expectations(userMessagePriorityService) {{
            userMessagePriorityService.getConfiguredServiceForRule(rule1);
            result = USER_MESSAGE_SERVICE;

            userMessagePriorityService.getConfiguredActionForRule(rule1);
            result = USER_MESSAGE_ACTION;

            userMessagePriorityService.matchesServiceAndAction(USER_MESSAGE_SERVICE, USER_MESSAGE_ACTION, USER_MESSAGE_SERVICE, USER_MESSAGE_ACTION);
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = DomainService.DEFAULT_DOMAIN;

            userMessagePriorityService.getConfiguredPriorityForRule(DomainService.DEFAULT_DOMAIN, rule1);
            result = priorityValue;
        }};

        assertEquals(priorityValue, userMessagePriorityService.getMatchingPriority(rule1, USER_MESSAGE_SERVICE, USER_MESSAGE_ACTION));
    }

    @Test
    public void matchesServiceAndActionWithBothValues() {
        String userMessageService = "message service";
        String userMessageAction = "message action";
        String servicePropertyValue = "service property value";
        String actionPropertyValue = "action property value";

        new Expectations(userMessagePriorityService) {{
            userMessagePriorityService.matchesService(userMessageService, servicePropertyValue);
            result = true;

            userMessagePriorityService.matchesActionList(userMessageAction, actionPropertyValue);
            result = true;
        }};

        assertEquals(true, userMessagePriorityService.matchesServiceAndAction(userMessageService, userMessageAction, servicePropertyValue, actionPropertyValue));

        new Verifications() {{
            userMessagePriorityService.matchesService(userMessageService, servicePropertyValue);
            times = 1;

            userMessagePriorityService.matchesActionList(userMessageAction, actionPropertyValue);
            times = 1;
        }};
    }

    @Test
    public void matchesServiceAndActionWithOnlyService() {
        String userMessageService = "message service";
        String userMessageAction = "message action";
        String servicePropertyValue = "service property value";


        new Expectations(userMessagePriorityService) {{
            userMessagePriorityService.matchesService(userMessageService, servicePropertyValue);
            result = true;
        }};

        assertEquals(true, userMessagePriorityService.matchesServiceAndAction(userMessageService, userMessageAction, servicePropertyValue, null));

        new Verifications() {{
            userMessagePriorityService.matchesService(userMessageService, servicePropertyValue);
            times = 1;

            userMessagePriorityService.matchesActionList(userMessageAction, null);
            times = 0;
        }};
    }

    @Test
    public void matchesService() {
        String userMessageService = "message service";
        String servicePropertyValue = userMessageService;

        assertTrue(userMessagePriorityService.matchesService(userMessageService, servicePropertyValue));
    }

    @Test
    public void matchesActionList() {
        String userMessageAction = "action1";
        String actionPropertyValue = "action1,action2";

        new Expectations() {{
            userMessagePriorityService.matchesAction(userMessageAction, "action1");
        }};

        assertTrue(userMessagePriorityService.matchesActionList(userMessageAction, actionPropertyValue));
    }

    @Test
    public void matchesAction() {
        String userMessageAction = "action1";
        String actionPropertyValue = userMessageAction;

        assertTrue(userMessagePriorityService.matchesAction(userMessageAction, actionPropertyValue));
    }

    @Test
    public void getPriorityPropertyName() {
        String expected = "dispatcher.priority.medium.value";
        assertEquals(expected, userMessagePriorityService.getPriorityPropertyName("dispatcher.priority", "medium", "value"));
    }


    @Test
    public void getConfiguredRulesWithConcurrency(@Injectable Domain domain,
                                                  @Injectable UserMessagePriorityConfiguration priorityConfiguration) {
        String rule1 = "rule1";
        List<String> priorityRuleNames = Arrays.asList(rule1);

        new Expectations(userMessagePriorityService) {{
            domibusPropertyProvider.getNestedProperties(domain, DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY);
            result = priorityRuleNames;

            userMessagePriorityService.getPriorityConfiguration(domain, rule1);
            result = priorityConfiguration;

            priorityConfiguration.getConcurrencyPropertyName();
            result = "1-6";
        }};

        List<UserMessagePriorityConfiguration> configuredRulesWithConcurrency = userMessagePriorityService.getConfiguredRulesWithConcurrency(domain);
        Assert.assertEquals(1, configuredRulesWithConcurrency.size());
        Assert.assertEquals(priorityConfiguration, configuredRulesWithConcurrency.iterator().next());
    }

    @Test
    public void getPriorityConfiguration(@Injectable Domain domain) {
        String priorityRuleName = "rule1";
        String priority = "3";
        String concurrency = "dispatcher.rule1";

        new Expectations(userMessagePriorityService) {{
            userMessagePriorityService.getConfiguredPriorityForRule(domain, priorityRuleName);
            result = priority;

            userMessagePriorityService.getPriorityAsInteger(priority);
            result = 3;

            userMessagePriorityService.getConcurrencyPropertyName(priorityRuleName);
            result = concurrency;
        }};

        UserMessagePriorityConfiguration priorityConfiguration = userMessagePriorityService.getPriorityConfiguration(domain, priorityRuleName);
        assertEquals(priorityRuleName, priorityConfiguration.getRuleName());
        assertEquals(Integer.valueOf(priority), priorityConfiguration.getPriority());
        assertEquals(concurrency, priorityConfiguration.getConcurrencyPropertyName());
    }

}