package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.GatewayConfigurationValidator;
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
    MessageListenerContainerInitializer messageListenerContainerInitializer;

    @Autowired
    EArchiveFileStorageProvider eArchiveFileStorageProvider;

    @Autowired
    StaticDictionaryService staticDictionaryService;

    @Autowired
    PayloadEncryptionService payloadEncryptionService;

    @Autowired
    PayloadFileStorageProvider payloadFileStorageProvider;

    @Autowired
    BackendFilterInitializerService backendFilterInitializerService;

    @Autowired
    GatewayConfigurationValidator gatewayConfigurationValidator;

    @Autowired
    PasswordEncryptionService passwordEncryptionService;

    @Autowired
    MultiDomainCryptoService multiDomainCryptoService;

    @Autowired
    TLSCertificateManager tlsCertificateManager;

    @Autowired
    DomibusScheduler domibusScheduler;

    @Autowired
    DomibusCacheService domibusCacheService;

    @Override
    public void handleDomainsChanged() {
        if (domibusConfigurationService.isSingleTenantAware()) {
            return;
        }

        List<Domain> addedDomains = getAddedDomains();
        if (addedDomains.isEmpty()) {
            return;
        }

        addedDomains.forEach(domain -> {
            // now the domain title property is loaded in domibus property provider
            loadProperties(domain);

            domibusCacheService.evict(DomibusCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderDispatcher.getCacheKeyValue(domain, DOMAIN_TITLE));
            domain.setName(domainDao.getDomainTitle(domain));

            // let's see if order counts, otherwise we might inject a list of DomainAware instead
            messageListenerContainerInitializer.domainAdded(domain);
            eArchiveFileStorageProvider.domainAdded(domain);
            staticDictionaryService.domainAdded(domain);
            multiDomainCryptoService.domainAdded(domain);
            tlsCertificateManager.domainAdded(domain);
            payloadEncryptionService.domainAdded(domain);
            payloadFileStorageProvider.domainAdded(domain);
            backendFilterInitializerService.domainAdded(domain);
            gatewayConfigurationValidator.domainAdded(domain);
            passwordEncryptionService.domainAdded(domain);
            domibusScheduler.domainAdded(domain);
        });

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
