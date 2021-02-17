package eu.domibus.core.property.encryption.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.property.encryption.PasswordEncryptionContextFactory;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.ext.services.PluginPasswordEncryptionContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionExtServiceImpl implements PasswordEncryptionExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionExtServiceImpl.class);

    protected final PasswordEncryptionService passwordEncryptionService;

    protected final PasswordDecryptionService passwordDecryptionService;

    protected final PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    protected final DomibusCoreMapper coreMapper;

    public PasswordEncryptionExtServiceImpl(
            PasswordEncryptionService passwordEncryptionService,
            PasswordEncryptionContextFactory passwordEncryptionContextFactory,
            PasswordDecryptionService passwordDecryptionService,
            DomibusCoreMapper coreMapper) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.passwordDecryptionService = passwordDecryptionService;
        this.passwordEncryptionContextFactory = passwordEncryptionContextFactory;
        this.coreMapper = coreMapper;
    }

    @Override
    public void encryptPasswordsInFile(PluginPasswordEncryptionContext pluginPasswordEncryptionContext) {
        LOG.debug("Encrypting passwords in file");

        final Domain domain = coreMapper.domainDTOToDomain(pluginPasswordEncryptionContext.getDomain());
        LOG.debug("Using domain [{}]", domain);

        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
        PasswordEncryptionContext encryptionContext = new PluginPasswordEncryptionContextDelegate(pluginPasswordEncryptionContext, passwordEncryptionContext);
        passwordEncryptionService.encryptPasswords(encryptionContext);
    }

    @Override
    public boolean isValueEncrypted(String propertyValue) {
        return passwordEncryptionService.isValueEncrypted(propertyValue);
    }

    @Override
    public String decryptProperty(DomainDTO domainDTO, String propertyName, String encryptedFormatValue) {
        LOG.debug("Decrypting property [{}] for domain [{}]", propertyName, domainDTO);

        final Domain domain = coreMapper.domainDTOToDomain(domainDTO);
        return passwordDecryptionService.decryptProperty(domain, propertyName, encryptedFormatValue);
    }

    @Override
    public PasswordEncryptionResultDTO encryptProperty(DomainDTO domainDTO, String propertyName, String encryptedFormatValue) {
        LOG.debug("Encrypting property [{}] for domain [{}]", propertyName, domainDTO);

        final Domain domain = coreMapper.domainDTOToDomain(domainDTO);
        final PasswordEncryptionResult passwordEncryptionResult = passwordEncryptionService.encryptProperty(domain, propertyName, encryptedFormatValue);
        return coreMapper.passwordEncryptionResultToPasswordEncryptionResultDTO (passwordEncryptionResult);
    }
}
