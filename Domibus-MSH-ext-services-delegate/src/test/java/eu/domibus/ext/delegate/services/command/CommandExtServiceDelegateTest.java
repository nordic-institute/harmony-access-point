package eu.domibus.ext.delegate.services.command;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class CommandExtServiceDelegateTest {

    @Tested
    private CommandExtServiceDelegate commandExtServiceDelegate;

    @Injectable
    protected SignalService signalService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Test
    public void signalCommand(@Injectable Domain currentDomain) {
        Map<String, String> properties = new HashMap<>();
        String commandName = "mycommand";
        String domain = "mydomain";

        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = currentDomain;

            currentDomain.getCode();
            result = domain;

        }};

        commandExtServiceDelegate.executeCommand(commandName, properties);

        new Verifications() {{
            properties.put(Command.COMMAND, commandName);
            properties.put(MessageConstants.DOMAIN, domain);

            Map<String, String> commandProperties = null;
            signalService.sendMessage(commandProperties = withCapture());

            assertEquals(commandName, commandProperties.get(Command.COMMAND));
            assertEquals(domain, commandProperties.get(MessageConstants.DOMAIN));
        }};
    }
}