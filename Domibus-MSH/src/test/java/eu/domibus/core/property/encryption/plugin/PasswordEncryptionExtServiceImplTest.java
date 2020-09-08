package eu.domibus.core.property.encryption.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.encryption.PasswordEncryptionContextFactory;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.domibus.ext.services.PluginPasswordEncryptionContext;
import mockit.*;
import org.junit.Test;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
@SuppressWarnings("TestMethodWithIncorrectSignature")
public class PasswordEncryptionExtServiceImplTest {

    @Tested
    protected PasswordEncryptionExtServiceImpl passwordEncryptionExtService;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Injectable
    protected DomainExtConverter domainExtConverter;

    @Test
    public void encryptPasswordsInFile(@Injectable PluginPasswordEncryptionContext pluginPasswordEncryptionContext,
                                       @Injectable Domain domain,
                                       @Injectable PasswordEncryptionContext passwordEncryptionContext,
                                       @Mocked PluginPasswordEncryptionContextDelegate pluginPasswordEncryptionContextDelegate) {
        new Expectations() {{
            domainExtConverter.convert(pluginPasswordEncryptionContext.getDomain(), Domain.class);
            result = domain;

            passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
            result = passwordEncryptionContext;

            new PluginPasswordEncryptionContextDelegate(pluginPasswordEncryptionContext, passwordEncryptionContext);
            result = pluginPasswordEncryptionContextDelegate;
        }};

        passwordEncryptionExtService.encryptPasswordsInFile(pluginPasswordEncryptionContext);

        new FullVerifications() {{
            passwordEncryptionService.encryptPasswords(pluginPasswordEncryptionContextDelegate);
        }};
    }

    @Test
    public void isValueEncrypted() {
        String propertyValue = "";

        passwordEncryptionExtService.isValueEncrypted(propertyValue);

        new FullVerifications() {{
            passwordEncryptionService.isValueEncrypted(propertyValue);
        }};
    }

    @Test
    public void decryptProperty(@Injectable DomainDTO domainDTO,
                                @Injectable String propertyName,
                                @Injectable String encryptedFormatValue,
                                @Injectable Domain domain) {
        new Expectations() {{
            domainExtConverter.convert(domainDTO, Domain.class);
            result = domain;
        }};

        passwordEncryptionExtService.decryptProperty(domainDTO, propertyName, encryptedFormatValue);

        new FullVerifications() {{
            passwordEncryptionService.decryptProperty(domain, propertyName, encryptedFormatValue);
        }};
    }

    @Test
    public void encryptProperty(@Injectable DomainDTO domainDTO,
                                @Injectable String propertyName,
                                @Injectable String encryptedFormatValue,
                                @Injectable Domain domain,
                                @Injectable PasswordEncryptionResult passwordEncryptionResult) {
        new Expectations() {{
            domainExtConverter.convert(domainDTO, Domain.class);
            result = domain;

            passwordEncryptionService.encryptProperty(domain, propertyName, encryptedFormatValue);
            result = passwordEncryptionResult;
        }};

        passwordEncryptionExtService.encryptProperty(domainDTO, propertyName, encryptedFormatValue);

        new FullVerifications() {{
            domainExtConverter.convert(passwordEncryptionResult, PasswordEncryptionResultDTO.class);
        }};
    }
}