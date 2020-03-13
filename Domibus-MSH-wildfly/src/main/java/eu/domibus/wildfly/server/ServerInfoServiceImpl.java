package eu.domibus.wildfly.server;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ServerInfoServiceImpl.class);

    public static final String SERVER_NAME = "jboss.server.name";
    public static final String NODE_NAME = "jboss.node.name";

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    public String getServerName() {
        final String serverName = System.getenv(domibusConfigurationService.isClusterDeployment() ? NODE_NAME : SERVER_NAME);
        LOG.debug("serverName={}", serverName);

        return serverName;
    }


}
