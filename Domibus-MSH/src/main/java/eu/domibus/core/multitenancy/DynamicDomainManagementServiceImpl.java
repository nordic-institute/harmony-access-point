package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.crypto.api.TLSCertificateManager;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DynamicDomainManagementServiceImpl implements DynamicDomainManagementService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    private List<Domain> originalDomains;

    @Autowired
    private DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;


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
    AnnotationConfigWebApplicationContext rootContext;

    @PostConstruct
    public void init() {
        originalDomains = domainService.getDomains();
    }

    @Override
    public void handleDomainsChanged() {
        if (domibusConfigurationService.isSingleTenantAware()) {
            return;
        }

        domainService.resetDomains();
        List<Domain> currentList = domainService.getDomains();
        List<Domain> addedDomains = currentList.stream()
                .filter(el -> !originalDomains.contains(el))
                .collect(Collectors.toList());

        if (addedDomains.isEmpty()) {
            return;
        }

        loadProperties(addedDomains);

        // let's see if order counts, otherwise we might inject a list of DomainAware instead
        messageListenerContainerInitializer.domainsChanged(addedDomains, null);
        eArchiveFileStorageProvider.domainsChanged(addedDomains, null);
        staticDictionaryService.domainsChanged(addedDomains, null);
        multiDomainCryptoService.domainsChanged(addedDomains, null);
        tlsCertificateManager.domainsChanged(addedDomains, null);
        payloadEncryptionService.domainsChanged(addedDomains, null);
        payloadFileStorageProvider.domainsChanged(addedDomains, null);
        backendFilterInitializerService.domainsChanged(addedDomains, null);
        gatewayConfigurationValidator.domainsChanged(addedDomains, null);
        passwordEncryptionService.domainsChanged(addedDomains, null);
        domibusScheduler.domainsChanged(addedDomains, null);
    }

    private void loadProperties(List<Domain> addedDomains) {
        // import the new properties files
        // TODO move elsewhere??
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        addedDomains.stream().forEach(domain -> {
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
        });
    }

}
