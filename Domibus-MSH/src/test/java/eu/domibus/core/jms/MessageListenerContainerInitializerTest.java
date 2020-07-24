package eu.domibus.core.jms;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.jms.multitenancy.DomainMessageListenerContainerFactory;
import eu.domibus.core.message.UserMessagePriorityConfiguration;
import eu.domibus.core.message.UserMessagePriorityService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_CONCURENCY;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MessageListenerContainerInitializerTest {

    @Injectable
    ApplicationContext applicationContext;

    @Injectable
    protected DomainMessageListenerContainerFactory messageListenerContainerFactory;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainExtConverter domainConverter;

    @Injectable
    protected UserMessagePriorityService userMessagePriorityService;

    @Tested
    MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Test
    public void createSendMessageListenerContainers(@Injectable Domain domain,
                                                    @Injectable UserMessagePriorityConfiguration userMessagePriorityConfiguration) {
        List<UserMessagePriorityConfiguration> configuredPrioritiesWithConcurrency = new ArrayList<>();
        configuredPrioritiesWithConcurrency.add(userMessagePriorityConfiguration);

        Integer priority = 4;
        String selector = "my selector";
        String defaultSelector = "my default selector";
        String ruleName = "rule1";
        String concurrencyProperty = "dispatcher.rule";
        List<Integer> configuredPriorities = new ArrayList<>();
        configuredPriorities.add(priority);

        new Expectations(messageListenerContainerInitializer) {{
            userMessagePriorityService.getConfiguredRulesWithConcurrency(domain);
            result = configuredPrioritiesWithConcurrency;

            userMessagePriorityConfiguration.getPriority();
            result = 4;

            messageListenerContainerInitializer.getSelectorForPriority(priority);
            result = selector;

            userMessagePriorityConfiguration.getRuleName();
            result = ruleName;

            userMessagePriorityConfiguration.getConcurrencyPropertyName();
            result = concurrencyProperty;

            messageListenerContainerInitializer.getPriorities(configuredPrioritiesWithConcurrency);
            result = configuredPriorities;

            messageListenerContainerInitializer.getSelectorForDefaultDispatcher(configuredPriorities);
            result = defaultSelector;

            messageListenerContainerInitializer.createMessageListenersWithPriority((Domain) any, anyString, anyString, anyString);
        }};

        messageListenerContainerInitializer.createSendMessageListenerContainers(domain);

        new Verifications() {{
            messageListenerContainerInitializer.createMessageListenersWithPriority(domain, ruleName + "dispatcher", selector, concurrencyProperty);
            messageListenerContainerInitializer.createMessageListenersWithPriority(domain, "dispatchContainer", defaultSelector, DOMIBUS_DISPATCHER_CONCURENCY);
        }};
    }

    @Test
    public void getSelectorForPriority() {
        String selectorForPriority = messageListenerContainerInitializer.getSelectorForPriority(5);
        assertEquals("JMSPriority=5", selectorForPriority);
    }

    @Test
    public void getSelectorForDefaultDispatcher() {
        List<Integer> priorities = new ArrayList<>();
        priorities.add(1);
        priorities.add(3);

        String selectorForDefaultDispatcher = messageListenerContainerInitializer.getSelectorForDefaultDispatcher(priorities);
        assertEquals("(JMSPriority is null or (JMSPriority<>1 and JMSPriority<>3) )", selectorForDefaultDispatcher);
    }
}