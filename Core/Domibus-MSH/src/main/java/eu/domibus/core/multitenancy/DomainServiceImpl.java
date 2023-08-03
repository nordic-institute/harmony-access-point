package eu.domibus.core.multitenancy;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainServiceImpl.class);

    private static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "schedulerFactoryBean";

    private List<Domain> domains;

    private Object domainsLock = new Object();

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final DomainDao domainDao;

    private final DomibusLocalCacheService domibusLocalCacheService;

    private final DbSchemaUtil dbSchemaUtil;

    public DomainServiceImpl(DomibusPropertyProvider domibusPropertyProvider,
                             DomibusConfigurationService domibusConfigurationService,
                             DomainDao domainDao,
                             DomibusLocalCacheService domibusLocalCacheService,
                             DbSchemaUtil dbSchemaUtil) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainDao = domainDao;
        this.domibusLocalCacheService = domibusLocalCacheService;
        this.dbSchemaUtil = dbSchemaUtil;
    }

    @Override
    public List<Domain> getDomains() {
        if (domains == null) {
            synchronized (domainsLock) {
                if (domains == null) {
                    domains = getAllValidDomains();
                }
            }
        }
        return domains;
    }

    @Override
    public List<Domain> getAllValidDomains() {
        LOG.debug("Getting all potential domains that have a valid database schema.");
        return domainDao.findAll().stream()
                .filter(domain -> {
                    boolean valid = dbSchemaUtil.isDatabaseSchemaForDomainValid(domain);
                    if (!valid) {
                        LOG.info("Domain [{}] has invalid database schema so it will be filtered out.", domain);
                    }
                    return valid;
                })
                .collect(Collectors.toList());
    }

    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DomibusLocalCacheService.DOMAIN_BY_CODE_CACHE)
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

    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DomibusLocalCacheService.DOMAIN_BY_SCHEDULER_CACHE, key = "#schedulerName")
    @Override
    public Domain getDomainForScheduler(String schedulerName) {
        if (DEFAULT_QUARTZ_SCHEDULER_NAME.equalsIgnoreCase(schedulerName)) {
            return DomainService.DEFAULT_DOMAIN;
        }
        return getDomain(schedulerName);
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
            LOG.warn("Could not find domain [{}] to refresh.", domainCode);
            return;
        }

        domainDao.refreshDomain(domain);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.DOMAIN_BY_CODE_CACHE);
    }

    @Override
    public void addDomain(Domain domain) {
        if (domain == null) {
            LOG.info("Could not add a null domain.");
            return;
        }

        clearCaches(domain);
        if (!dbSchemaUtil.isDatabaseSchemaForDomainValid(domain)) {
            throw new DomibusDomainException(String.format("Cannot add domain [%s] because it does not have a valid database schema.", domain));
        }

        LOG.debug("Adding domain [{}]", domain);
        domains.add(domain);
    }

    @Override
    public void removeDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            LOG.info("Could not remove an empty domain.");
            return;
        }
        Domain domain = findByCode(domainCode, domains);
        if (domain == null) {
            LOG.warn("Could not find domain [{}] to remove.", domainCode);
            return;
        }

        LOG.debug("Removing domain [{}]", domain);
        domains.remove(domain);
        clearCaches(domain);
    }

    @Override
    public void validateDomain(String domainCode) {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return;
        }

        if (StringUtils.isEmpty(domainCode)) {
            throw new DomibusDomainException("Domain is empty.");
        }
        findByCode(domainCode, getDomains());
    }

    private Domain findByCode(String domainCode, List<Domain> allDomains) {
        return allDomains.stream().filter(el -> StringUtils.equalsIgnoreCase(el.getCode(), domainCode))
                .findAny().orElseThrow(() -> new DomibusDomainException(String.format("Domain [%s] is not in the list of valid domains. Please check the domain configuration and database schema.", domainCode)));
    }

    private void clearCaches(Domain domain) {
        LOG.info("Clear db schema and domain by code caches for domain [{}]", domain);
        dbSchemaUtil.removeCachedDatabaseSchema(domain);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.DOMAIN_BY_CODE_CACHE);
    }

}
