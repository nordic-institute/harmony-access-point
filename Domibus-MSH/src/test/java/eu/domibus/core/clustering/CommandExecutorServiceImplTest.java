package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.ext.services.CommandExtTask;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author idragusa
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class CommandExecutorServiceImplTest {

    @Tested
    private CommandExecutorServiceImpl commandExecutorService;

    @Injectable
    protected CommandService commandService;

    @Injectable
    protected ServerInfoService serverInfoService;

    @Injectable
    protected List<CommandTask> commandTasks;

    @Injectable
    protected List<CommandExtTask> pluginCommands;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Test
    public void testExecuteCommands(@Mocked Command command1, @Mocked Command command2) {
        String server1 = "server1";
        List<Command> commands = new ArrayList<>();
        commands.add(command1);
        commands.add(command2);


        new NonStrictExpectations(commandExecutorService) {{
            command1.getCommandName();
            result = Command.RELOAD_PMODE;

            command2.getCommandName();
            result = Command.RELOAD_TRUSTSTORE;

            commandService.findCommandsByServerName(server1);
            result = commands;

        }};

        commandExecutorService.executeCommands(server1);

        new Verifications() {{
            commandExecutorService.executeAndDeleteCommand(command1);
            times = 1;

            commandExecutorService.executeAndDeleteCommand(command2);
            times = 1;
        }};
    }

    @Test
    public void executeCommand(@Injectable Domain domain,
                               @Injectable Map<String, String> commandProperties,
                               @Injectable CommandTask commandTask) {
        String command = "mycommand";

        new Expectations(commandExecutorService) {{
            commandExecutorService.skipCommandSameServer(command, commandProperties);
            result = false;

            commandExecutorService.getCommandTask(command);
            result = commandTask;
        }};

        commandExecutorService.executeCommand(command, commandProperties);

        new Verifications() {{
            commandTask.execute(commandProperties);
            times = 1;

            commandExecutorService.getPluginCommand(command);
            times = 0;
        }};
    }

    @Test
    public void executePluginCommand(@Injectable Map<String, String> commandProperties,
                                     @Injectable CommandExtTask commandTask) {
        String command = "mycommand";

        new Expectations(commandExecutorService) {{
            commandExecutorService.skipCommandSameServer(command, commandProperties);
            result = false;

            commandExecutorService.getCommandTask(command);
            result = null;

            commandExecutorService.getPluginCommand(command);
            result = commandTask;
        }};

        commandExecutorService.executeCommand(command, commandProperties);

        new Verifications() {{
            commandTask.execute(commandProperties);
            times = 1;
        }};
    }

    @Test
    public void executeAndDeleteCommand(@Injectable Command commandTask) {
        new Expectations(commandExecutorService) {{
            commandExecutorService.executeCommand(commandTask.getCommandName(), commandTask.getCommandProperties());
        }};

        commandExecutorService.executeAndDeleteCommand(commandTask);

        new Verifications() {{
            commandService.deleteCommand(commandTask.getEntityId());
        }};
    }

    @Test
    public void skipCommandSameServer(@Injectable Map<String, String> commandProperties,
                                      @Injectable CommandExtTask commandTask) {
        String command = "mycommand";
        String originServerName = "server1";

        new Expectations() {{
            commandProperties.get(CommandProperty.ORIGIN_SERVER);
            result = originServerName;

            serverInfoService.getServerName();
            result = originServerName;
        }};

        assertTrue(commandExecutorService.skipCommandSameServer(command, commandProperties));
    }
}
