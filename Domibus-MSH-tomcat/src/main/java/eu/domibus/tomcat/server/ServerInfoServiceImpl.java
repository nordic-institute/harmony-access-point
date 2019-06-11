package eu.domibus.tomcat.server;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Set;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ServerInfoServiceImpl.class);

    private static final String DOMIBUS_NODE_ID = "domibus.node.id";

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Override
    public String getServerName() {
        //we are getting this using JMX
        //the value is set in server.xml - <Host> section
        String serverName = null;
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        try {

            final ObjectName filterName = new ObjectName("Catalina:type=Host,host=*");
            Set<ObjectName> nameSet = server.queryNames(filterName, null);

            if (CollectionUtils.isNotEmpty(nameSet)) {
                final ObjectName objectName = nameSet.iterator().next();
                serverName = (String) server.getAttribute(objectName, "name");
            }
        } catch (MalformedObjectNameException | ReflectionException |
                InstanceNotFoundException | AttributeNotFoundException | MBeanException e) {
            LOG.warn("Unable to get server name using JMX: ", e);
        }

        if (domibusConfigurationService.isClusterDeployment()) {
            //we are appending the domibus.node.id defined in setenv.bat/sh file
            serverName += System.getProperty(DOMIBUS_NODE_ID);
        }

        return serverName;
    }
}
