package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.DomainService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.*;

/**
 * @author idragusa
 * @since 4.2
 */
@RunWith(JMockit.class)
public class CommandExecutorServiceImplTest {


    @Injectable
    private CommandService commandService;

    @Tested
    private CommandExecutorServiceImpl commandExecutorService;

    @Test
    public void testExecuteCommands(@Mocked Command command1, @Mocked Command command2) {
        String server1 = "server1";
        String server2 = "server2";
        List<Command> commands = new ArrayList<>();
        commands.add(command1);
        commands.add(command2);


        new NonStrictExpectations() {{
            command1.getCommandName(); result = Command.RELOAD_PMODE;
            command2.getCommandName(); result = Command.RELOAD_TRUSTSTORE;
            commandService.findCommandsByServerAndDomainName(server1, DomainService.DEFAULT_DOMAIN.getCode());
            result = commands;

            commandService.findCommandsByServerAndDomainName(server2, DomainService.DEFAULT_DOMAIN.getCode());
            result = null;
        }};

        commandExecutorService.executeCommands(server1, DomainService.DEFAULT_DOMAIN);
        commandExecutorService.executeCommands(server2, DomainService.DEFAULT_DOMAIN);

        new Verifications() {{
            commandService.executeAndDeleteCommand(command1, DomainService.DEFAULT_DOMAIN);
            times = 1;
            commandService.executeAndDeleteCommand(command2, DomainService.DEFAULT_DOMAIN);
            times = 1;
        }};
    }
}
