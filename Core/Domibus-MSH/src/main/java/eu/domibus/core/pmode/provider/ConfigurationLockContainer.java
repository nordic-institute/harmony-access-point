package eu.domibus.core.pmode.provider;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigurationLockContainer {
    private static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConfigurationLockContainer.class);
    private static final ConcurrentMap<String, Object> domainSpecificConfigurationLock = new ConcurrentHashMap<>();

    private ConfigurationLockContainer() {
    }

    public static Object getForDomain(Domain domain) {
        String key;
        if (domain == null) {
            key = "default";
        } else {
            key = domain.getCode();
        }
        if (!domainSpecificConfigurationLock.containsKey(key)) {
            domainSpecificConfigurationLock.putIfAbsent(key, new Object());
        }
        Object lock = domainSpecificConfigurationLock.get(key);
        LOG.debug("Lock for domain [{}] is [{}]", key, lock);
        return lock;
    }
}
