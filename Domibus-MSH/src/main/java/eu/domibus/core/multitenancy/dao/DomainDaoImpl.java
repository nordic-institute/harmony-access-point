package eu.domibus.core.multitenancy.dao;


import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Component
public class DomainDaoImpl implements DomainDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainDaoImpl.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;
    protected static final String DOMAIN_NAME_REGEX = "^[a-z0-9_]*$";

    @Cacheable(value = DomibusCacheService.ALL_DOMAINS_CACHE)
    @Override
    public List<Domain> findAll() {
        LOG.trace("Finding all domains");

        List<Domain> result = new ArrayList<>();
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("Domibus is running in non multitenant mode, adding only the default domain");
            result.add(DomainService.DEFAULT_DOMAIN);
            return result;
        }

        List<String> domainCodes = findAllDomainCodes();
        if (domainCodes == null) {
            return result;
        }

        List<Domain> domains = new ArrayList<>();
        for (String domainCode : domainCodes) {
            if (isValidDomain(domains, domainCode)) {
                Domain domain = new Domain();
                domain.setCode(domainCode.toLowerCase());
                domain.setName(getDomainTitle(domain));
                domains.add(domain);
                LOG.trace("Domain name is valid. Added domain [{}]", domain);
            }
        }
        if (domains.stream().noneMatch(domain -> DomainService.DEFAULT_DOMAIN.equals(domain))) {
            LOG.warn("Default domain is normally present in the configuration.");
        }

        domains.sort(Comparator.comparing(Domain::getName, String.CASE_INSENSITIVE_ORDER));
        result.addAll(domains);

        LOG.trace("Found the following domains [{}]", result);

        return result;
    }

    protected List<String> findAllDomainCodes() {
        final String configLocation = domibusConfigurationService.getConfigLocation();
        File confDirectory = new File(configLocation + File.separator + DomainService.DOMAINS_HOME);
        final File[] domainHomes = confDirectory.listFiles(File::isDirectory);
        if (domainHomes == null) {
            LOG.warn("Invalid domains path: [{}]", confDirectory);
            return null;
        }
        return Arrays.stream(domainHomes).map(File::getName).collect(Collectors.toList());
    }

    protected boolean isValidDomain(List<Domain> domains, String domainCode) {
        if (Character.isDigit(domainCode.charAt(0))) {
            LOG.error("Domain name [{}] should not start with a number. It should start with a letter and contain only lower case letters, numbers and underscore.", domainCode);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Domain name should not start with a number. Invalid domain name:" + domainCode);
        }

        if (domains.stream().anyMatch(d -> d.getCode().equals(domainCode))) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Found duplicate domain name :" + domainCode);
        }
        if (!domainCode.matches(DOMAIN_NAME_REGEX)) {
            LOG.error("Domain name [{}] is not valid. It should start with a letter and contain only lower case letters, numbers and underscore.", domainCode);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Forbidden characters like capital letters or special characters, except underscore found in domain name. Invalid domain name:" + domainCode);
        } else {
            return true;
        }
    }

    protected String getDomainTitle(Domain domain) {
        String domainTitle = domibusPropertyProvider.getProperty(domain, DOMAIN_TITLE);
        if (StringUtils.isEmpty(domainTitle)) {
            domainTitle = domain.getCode();
        }
        return domainTitle;
    }
}

