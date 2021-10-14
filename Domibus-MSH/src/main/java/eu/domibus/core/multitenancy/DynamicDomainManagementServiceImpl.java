package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.PropertyProviderDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DynamicDomainManagementServiceImpl implements DynamicDomainManagementService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    @Autowired
    private DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainDao domainDao;

    @Autowired
    AnnotationConfigWebApplicationContext rootContext;

    @Autowired
    PropertyProviderDispatcher propertyProviderDispatcher;

    @Autowired
    DomibusCacheService domibusCacheService;

    @Autowired
    SignalService signalService;

    @Autowired
    List<DomainsAware> domainsAwareList;

    @Override
    public void checkAndHandleDomainsChanged() {
        if (domibusConfigurationService.isSingleTenantAware()) {
            return;
        }

        List<Domain> addedDomains = getAddedDomains();
        if (addedDomains.isEmpty()) {
            return;
        }

        addedDomains.forEach(domain -> {
            try {
                addDomain(domain);
            } catch (Exception ex) {
                //todo return a result type detailing the outcome for each domain
                LOG.error("Could not add domain [[]]!");
            }
        });

        //signal for other nodes in the cluster
        LOG.debug("Broadcasting dynamically adding domains [{}]", addedDomains);
        try {
            signalService.signalDomainsAdded();
        } catch (Exception ex) {
            throw new DomibusPropertyException("Exception signaling dynamically adding domains " + addedDomains, ex);
        }
    }

    private void addDomain(Domain domain) {

        loadProperties(domain);

        domibusCacheService.evict(DomibusCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderDispatcher.getCacheKeyValue(domain, DOMAIN_TITLE));
        domain.setName(domainDao.getDomainTitle(domain));

        domainsAwareList.forEach(el -> el.domainAdded(domain));
    }

    private List<Domain> getAddedDomains() {
        // todo looks non cohesive
        List<Domain> previousDomains = domainService.getDomains();
        domainService.resetDomains();
        List<Domain> currentDomains = domainService.getDomains();
        List<Domain> addedDomains = currentDomains.stream()
                .filter(el -> !previousDomains.contains(el))
                .collect(Collectors.toList());
        return addedDomains;
    }

    // import the new properties files
    // TODO move elsewhere??
    private void loadProperties(Domain domain) {
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        String configFile = domibusConfigurationService.getConfigLocation() + "/" + domibusConfigurationService.getConfigurationFileName(domain);
        LOG.debug("Loading properties file for domain [{}]: [{}]...", domain, configFile);
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            DomibusPropertiesPropertySource newPropertySource = new DomibusPropertiesPropertySource("propertiesOfDomain" + domain.getCode(), properties);
            propertySources.addLast(newPropertySource);
        } catch (IOException ex) {
            LOG.error("Could not read properties file: [{}]", configFile, ex);
            // TODO throw
        }

    }

}
