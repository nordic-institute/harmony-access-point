package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_SCHEMA;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainServiceImpl.class);

    private static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "schedulerFactoryBean";

    protected final Object generalSchemaLock = new Object();

    protected volatile String generalSchema;

    protected volatile Map<Domain, String> domainSchemas = new HashMap<>();

    private List<Domain> domains;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomainDao domainDao;

    private final DomibusCacheService domibusCacheService;

    private final AuthenticationService authenticationService;

    public DomainServiceImpl(DomibusPropertyProvider domibusPropertyProvider, DomainDao domainDao,
                             DomibusCacheService domibusCacheService, @Lazy AuthenticationService authenticationService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainDao = domainDao;
        this.domibusCacheService = domibusCacheService;
        this.authenticationService = authenticationService;

        domains = domainDao.findAll();
    }

    @Override
    public synchronized List<Domain> getDomains() {
        LOG.debug("Getting active domains.");
        return domains;
    }

    @Override
    public List<Domain> getAllDomains() {
        LOG.debug("Getting all potential domains.");
        return domainDao.findAll();
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
                domainSchema = domainSchemas.get(domain);
                if (domainSchema == null) {
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
                    generalSchema = domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
                    LOG.debug("Caching general schema [{}]", generalSchema);
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
    public void refreshDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            LOG.info("Could not refresh an empty domain.");
            return;
        }
        Domain domain = domains.stream().filter(el -> StringUtils.equals(el.getCode(), domainCode)).findFirst().orElse(null);
        if (domain == null) {
            LOG.info("Could not find domain [{}] to refresh.", domainCode);
            return;
        }
        domainDao.refreshDomain(domain);
        this.domibusCacheService.clearCache(DomibusCacheService.DOMAIN_BY_CODE_CACHE);
    }

    @Override
    public void addDomain(Domain domain) {
        if (domain == null) {
            LOG.info("Could not add a null domain.");
            return;
        }
        LOG.debug("Adding domain [{}]", domain);
        domains.add(domain);

        authenticationService.addDomainCode(domain.getCode());
    }

    @Override
    public void removeDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            LOG.info("Could not remove an empty domain.");
            return;
        }
        Domain domain = domains.stream().filter(el -> StringUtils.equals(el.getCode(), domainCode)).findFirst().orElse(null);
        if (domain == null) {
            LOG.info("Could not find domain [{}] to remove.", domainCode);
            return;
        }
        domains.remove(domain);

        authenticationService.removeDomainCode(domain.getCode());
    }

}
