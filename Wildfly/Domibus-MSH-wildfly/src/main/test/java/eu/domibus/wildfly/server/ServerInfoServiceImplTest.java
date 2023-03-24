package eu.domibus.wildfly.server;

import eu.domibus.api.property.DomibusConfigurationService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@RunWith(JMockit.class)
public class ServerInfoServiceImplTest {

    @Tested
    ServerInfoServiceImpl serverInfoService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;


    @Test
    public void testGetServerName() {
        new Expectations() {{
            domibusConfigurationService.isClusterDeployment();
            result = false;
        }};
        serverInfoService.getServerName();

        new Verifications() {{
            System.getenv(ServerInfoServiceImpl.SERVER_NAME);
        }};


    }

}