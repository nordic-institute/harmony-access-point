package eu.domibus.core.plugin.routing;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.DomainContextProvider;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageFilterUpdatedCommandTaskTest {

    @Tested
    MessageFilterUpdatedCommandTask messageFilterUpdatedCommandTask;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected RoutingService routingService;

    @Test
    public void canHandle() {
        assertTrue(messageFilterUpdatedCommandTask.canHandle(Command.MESSAGE_FILTER_UPDATE));
    }

    @Test
    public void execute() {
        messageFilterUpdatedCommandTask.execute(null);

        new FullVerifications() {{
            routingService.invalidateBackendFiltersCache();
        }};

    }
}