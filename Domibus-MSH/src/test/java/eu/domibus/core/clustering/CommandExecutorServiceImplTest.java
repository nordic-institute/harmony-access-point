package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.ext.services.CommandExtTask;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author idragusa
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

            commandService.findCommandsByServerAndDomainName(server1, DomainService.DEFAULT_DOMAIN.getCode());
            result = commands;
        }};

        commandExecutorService.executeCommands(server1, DomainService.DEFAULT_DOMAIN);

        new Verifications() {{
            domainTaskExecutor.submit((Runnable) any, DomainService.DEFAULT_DOMAIN);
            times = 2;
        }};
    }
}
