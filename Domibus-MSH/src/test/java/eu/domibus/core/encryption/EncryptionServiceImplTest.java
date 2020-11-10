package eu.domibus.core.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1.3
 */
public class EncryptionServiceImplTest {

    @Injectable
    protected PayloadEncryptionService payloadEncryptionService;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Tested
    EncryptionServiceImpl encryptionService;

    @Test
    public void isAnyEncryptionActiveWithGeneralEncryptionActive() {
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive();
            result = true;
        }};

        assertTrue(encryptionService.isAnyEncryptionActive());
    }

    @Test
    public void isAnyEncryptionActiveWithOneDomainActive(@Injectable Domain domain1) {
        List<Domain> domains = new ArrayList<>();
        domains.add(domain1);

        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive();
            result = false;

            domainService.getDomains();
            result = domains;

            domibusConfigurationService.isPayloadEncryptionActive(domain1);
            result = true;
        }};

        assertTrue(encryptionService.isAnyEncryptionActive());
    }

    @Test
    public void handleEncryption() {
        new Expectations(encryptionService) {{
            encryptionService.isAnyEncryptionActive();
            result = true;
            encryptionService.doHandleEncryption();
        }};

        encryptionService.handleEncryption();

        new Verifications() {{
            encryptionService.doHandleEncryption();
            times = 1;
        }};
    }

    @Test
    public void handleEncryption_not() {
        new Expectations(encryptionService) {{
            encryptionService.isAnyEncryptionActive();
            result = false;
        }};

        encryptionService.handleEncryption();

        new Verifications() {{
            encryptionService.doHandleEncryption();
            times = 0;
        }};
    }

}