package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainServiceImpl.class);

    private static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "schedulerFactoryBean";

    protected final Object generalSchemaLock = new Object();
    protected volatile String generalSchema;
    protected volatile Map<Domain, String> domainSchemas = new HashMap<>();

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainDao domainDao;

    @Autowired
    private DomibusCacheService domibusCacheService;

    private List<Domain> domains;

    @Override
    public synchronized List<Domain> getDomains() {
        if (domains == null) {
            domains = domainDao.findAll();
        }
        return domains;
    }

    @Cacheable(value = DomibusCacheService.DOMAIN_BY_CODE_CACHE)
    @Override
    public Domain getDomain(String code) {
        LOG.trace("Getting domain with code [{}]", code);

        final List<Domain> domains = getDomains();
        if (domains == null) {
            LOG.trace("No domains found");
            return null;
        }
        for (Domain domain : domains) {
            if (StringUtils.equalsIgnoreCase(code, domain.getCode())) {
                LOG.trace("Found domain [{}] for code [{}]", domain, code);
                return domain;
            }
        }
        LOG.trace("No domain found with code [{}]", code);
        return null;
    }

    @Cacheable(value = DomibusCacheService.DOMAIN_BY_SCHEDULER_CACHE, key = "#schedulerName")
    @Override
    public Domain getDomainForScheduler(String schedulerName) {
        if (DEFAULT_QUARTZ_SCHEDULER_NAME.equalsIgnoreCase(schedulerName)) {
            return DomainService.DEFAULT_DOMAIN;
        }
        return getDomain(schedulerName);
    }

    /**
     * Get database schema name for the domain. Uses a local cache. This mechanism should be removed when EDELIVERY-7353 it will be implemented
     *
     * @param domain
     * @return database schema name
     */
    @Override
    public String getDatabaseSchema(Domain domain) {
        String domainSchema = domainSchemas.get(domain);
        if (domainSchema == null) {
            synchronized (domainSchemas) {
                if (domainSchemas.get(domain) == null) {
                    String value = domibusPropertyProvider.getProperty(domain, DOMIBUS_DATABASE_SCHEMA);
                    LOG.debug("Caching domain schema [{}] for domain [{}]", value, domain);
                    domainSchemas.put(domain, value);
                    domainSchema = value;
                }
            }
        }

        return domainSchema;
    }

    /**
     * Get the configured general schema. Uses a local cache. This mechanism should be removed when EDELIVERY-7353 it will be implemented
     */
    @Override
    public String getGeneralSchema() {
        if (generalSchema == null) {
            synchronized (generalSchemaLock) {
                if (generalSchema == null) {
                    LOG.debug("Caching general schema [{}]", generalSchema);
                    generalSchema = domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
                }
            }
        }
        return generalSchema;
    }

    @Override
    public String getSchedulerName(Domain domain) {
        String result = domain.getCode();
        if (DomainService.DEFAULT_DOMAIN.equals(domain)) {
            //keep the same name used in Domibus 3.3.x in order not to break the backward compatibility; if scheduler name is changed, a DB migration script is needed
            result = DEFAULT_QUARTZ_SCHEDULER_NAME;
        }
        return result;
    }

    @Override
    public synchronized void resetDomains() {
        this.domains = null;
        this.domibusCacheService.clearCache(DomibusCacheService.ALL_DOMAINS_CACHE);
        this.domibusCacheService.clearCache(DomibusCacheService.DOMAIN_BY_CODE_CACHE);
    }
}
