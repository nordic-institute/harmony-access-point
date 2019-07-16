package eu.domibus.weblogic.server;

import eu.domibus.api.server.ServerInfoService;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    private static final String SERVER_NAME = "weblogic.Name";

    @Override
    public String getServerName() {
        return System.getProperty(SERVER_NAME);
    }

}
