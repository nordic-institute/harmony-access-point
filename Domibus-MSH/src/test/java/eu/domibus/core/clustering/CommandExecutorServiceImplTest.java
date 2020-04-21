package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.DomainService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
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
    public void testExecuteCommands() {
        String server1 = "server1";
        String server2 = "server2";
        List<Command> commands = new ArrayList<>();
        Command command1 = new Command();
        command1.setCommandName(Command.RELOAD_PMODE);
        commands.add(command1);

        Command command2 = new Command();
        command2.setCommandName(Command.RELOAD_TRUSTSTORE);
        commands.add(command2);


        new Expectations() {{
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
