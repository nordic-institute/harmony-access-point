package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Service
public class CommandServiceImpl implements CommandService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandServiceImpl.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    protected MultiDomainCryptoService multiDomainCryptoService;

    @Autowired
    protected LoggingService loggingService;

    @Autowired
    private ServerInfoService serverInfoService;

    @Autowired
    private List<DomibusPropertyManager> domibusPropertyManagers;

    @Override
    public void executeCommand(String command, Domain domain, Map<String, String> commandProperties) {

        //skip the command if runs on same server
        if (skipCommandSameServer(command, domain, commandProperties)) {
            return;
        }

        LOG.debug("Executing command [{}] for domain [{}] having properties [{}]", command, domain, commandProperties);

        switch (command) {
            case Command.RELOAD_PMODE:
                pModeProvider.refresh();
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            case Command.EVICT_CACHES:
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
            case Command.RELOAD_TRUSTSTORE:
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            case Command.LOGGING_RESET:
                loggingService.resetLogging();
                break;
            case Command.LOGGING_SET_LEVEL:
                final String level = commandProperties.get(CommandProperty.LOG_LEVEL);
                final String name = commandProperties.get(CommandProperty.LOG_NAME);
                loggingService.setLoggingLevel(name, level);
                break;
            case Command.DOMIBUS_PROPERTY_CHANGE:
                final String domainCode = commandProperties.get(MessageConstants.DOMAIN);
                final String propName = commandProperties.get(CommandProperty.PROPERTY_NAME);
                final String propVal = commandProperties.get(CommandProperty.PROPERTY_VALUE);
                for (DomibusPropertyManager domibusPropertyManager : domibusPropertyManagers) {
                    if (domibusPropertyManager.hasKnownProperty(propName)) {
                        try {
                            domibusPropertyManager.setKnownPropertyValue(domainCode, propName, propVal, false);
                        } catch (Exception ex) {
                            LOG.error("Error trying to set property [{}] with value [{}] on domain [{}]", propName, propVal, domainCode);
                        }
                    }
                }
                break;
            default:
                LOG.error("Unknown command received: {}", command);
        }
    }

    /**
     * Returns true if the commands is send to same server
     */
    protected boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties) {
        if (commandProperties == null) {
            //execute the command
            return false;
        }
        String originServerName = commandProperties.get(CommandProperty.ORIGIN_SERVER);
        if (StringUtils.isBlank(originServerName)) {
            return false;
        }

        final String serverName = serverInfoService.getServerName();

        if (serverName.equalsIgnoreCase(originServerName)) {
            LOG.debug("Command [{}] for domain [{}] not executed as origin and actual server signature is the same [{}]", command, domain, serverName);
            return true;
        }
        return false;
    }
}
