package eu.domibus.core.multitenancy.dao;


import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
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
    public static final String DOMAIN_DOMIBUS_PROPERTIES = "-domibus.properties";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;
    protected static final String DOMAIN_NAME_REGEX = "^[a-z0-9_]*$";

    //    @Cacheable(value = DomibusCacheService.ALL_DOMAINS_CACHE)
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
            checkValidDomain(domains, domainCode);

            Domain domain = new Domain();
            domain.setCode(domainCode.toLowerCase());
            domain.setName(getDomainTitle(domain));
            domains.add(domain);

            LOG.trace("Domain name is valid. Added domain [{}]", domain);
        }
        if (domains.stream().noneMatch(domain -> DomainService.DEFAULT_DOMAIN.equals(domain))) {
            LOG.warn("Default domain is normally present in the configuration.");
        }

        domains.sort(Comparator.comparing(Domain::getName, String.CASE_INSENSITIVE_ORDER));
        result.addAll(domains);

        LOG.trace("Found the following domains [{}]", result);

        return result;
    }

    protected String getDomainTitle(Domain domain) {
        String domainTitle = domibusPropertyProvider.getProperty(domain, DOMAIN_TITLE);
        if (StringUtils.isEmpty(domainTitle)) {
            domainTitle = domain.getCode();
        }
        return domainTitle;
    }

    protected List<String> findAllDomainCodes() {
        final String configLocation = domibusConfigurationService.getConfigLocation();
        File confDirectory = new File(configLocation + File.separator + DomainService.DOMAINS_HOME);
        final File[] domainHomes = confDirectory.listFiles(File::isDirectory);
        if (domainHomes == null) {
            LOG.warn("Invalid domains path: [{}]", confDirectory);
            return null;
        }
        LOG.debug("Domain directories identified:[{}]", Arrays.stream(domainHomes).map(File::getName).collect(Collectors.toList()));
        //filter the domain home directories that contain files with name '-domibus.properties' only
        List<String> filteredDomainHomes = Arrays.stream(domainHomes).filter(domainDir -> domainDir.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter(DOMAIN_DOMIBUS_PROPERTIES, IOCase.INSENSITIVE)).length > 0).map(File::getName).sorted().collect(Collectors.toList());
        LOG.debug("Filtered Domain Homes with property files:" + filteredDomainHomes);
        return filteredDomainHomes;
    }

    protected void checkValidDomain(List<Domain> domains, String domainCode) {
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
        }

        checkConfigFile(domainCode);
    }

    protected void checkConfigFile(String domainCode) {
        Domain domain = new Domain(domainCode, domainCode);
        String configFile = domibusConfigurationService.getConfigLocation() + File.separator + domibusConfigurationService.getConfigurationFileName(domain);
        File f = new File(configFile);
        if (!f.exists() || f.isDirectory()) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, String.format("The domain [%s] does not have a properties file defined.", domainCode));
        }
    }
}

