package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService, DomainsAware {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainServiceImpl.class);

    private static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "schedulerFactoryBean";

    protected volatile Map<Domain, String> domainSchemas = new HashMap<>();

    private List<Domain> domains;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final DomainDao domainDao;

    private final DomibusCacheService domibusCacheService;

    private final DbSchemaUtil dbSchemaUtil;

    private final AuthUtils authUtils;

    public DomainServiceImpl(DomibusPropertyProvider domibusPropertyProvider,
                             DomibusConfigurationService domibusConfigurationService,
                             DomainDao domainDao,
                             DomibusCacheService domibusCacheService,
                             DbSchemaUtil dbSchemaUtil,
                             AuthUtils authUtils) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainDao = domainDao;
        this.domibusCacheService = domibusCacheService;
        this.dbSchemaUtil = dbSchemaUtil;
        this.authUtils = authUtils;
    }

    @PostConstruct
    public void initialize() {
        domains = getAllValidDomains();
    }

    @Override
    public synchronized List<Domain> getDomains() {
        LOG.debug("Getting active domains.");

        return domains;
    }

    @Override
    public List<Domain> getAllValidDomains() {
        LOG.debug("Getting all potential domains that have a valid database schema.");
        List<Domain> domains = domainDao.findAll();
        domains.removeIf(domain -> {
            boolean isValid = !dbSchemaUtil.isDatabaseSchemaForDomainValid(domain);
            if (!isValid) {
                LOG.info("Domain [{}] has invalid database schema so it will be filtered out.", domain);
            }
            return isValid;
        });
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
        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_BY_CODE_CACHE);
        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_VALIDITY_CACHE);
    }

    @Override
    public void addDomain(Domain domain) {
        if (domain == null) {
            LOG.info("Could not add a null domain.");
            return;
        }
        LOG.debug("Adding domain [{}]", domain);
        domains.add(domain);

        authUtils.executeOnLoggedUser(userDetails -> userDetails.addDomainCode(domain.getCode()));

        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_BY_CODE_CACHE);
        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_VALIDITY_CACHE);
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
        domains.remove(domain);

        authUtils.executeOnLoggedUser(userDetails -> userDetails.removeDomainCode(domainCode));

        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_BY_CODE_CACHE);
        domibusCacheService.clearCache(DomibusCacheService.DOMAIN_VALIDITY_CACHE);
    }

    @Override
    public void validateDomain(String domainCode) {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return;
        }

        if (StringUtils.isEmpty(domainCode)) {
            throw new DomibusDomainException("Domain is empty.");
        }

        Domain domain = findByCode(domainCode, getDomains());
        if (domain == null) {
            throw new DomibusDomainException(String.format("Domain [%s] is not enabled.", domainCode));
        }
    }

    private Domain findByCode(String domainCode, List<Domain> allDomains) {
        return allDomains.stream().filter(el -> StringUtils.equalsIgnoreCase(el.getCode(), domainCode)).findFirst().orElse(null);
    }

    @Override
    public void onDomainAdded(Domain domain) {
        removeCachedSchema(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        removeCachedSchema(domain);
    }

    private void removeCachedSchema(Domain domain) {
        String domainSchema = domainSchemas.get(domain);
        if (domainSchema == null) {
            LOG.debug("Domain schema for domain [{}] not found; exiting", domain);
            return;
        }
        synchronized (domainSchemas) {
            domainSchema = domainSchemas.get(domain);
            if (domainSchema != null) {
                LOG.debug("Removing domain schema [{}] for domain [{}]", domainSchema, domain);
                domainSchemas.remove(domain);
            }
        }
    }
}
