package eu.domibus.tomcat.server;

import eu.domibus.api.server.ServerInfoService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    @Override
    public String getServerName() {
        System.getProperties();
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    @Override
    public String getNodeName() {
        return null;
    }

    @Override
    public String getHumanReadableServerName() {
        return getServerName().split("@")[1];
    }
}
