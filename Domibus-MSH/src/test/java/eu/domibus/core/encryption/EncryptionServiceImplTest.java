package eu.domibus.core.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.payload.encryption.PayloadEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Tested
    EncryptionServiceImpl encryptionService;

    @Test
    public void useLockForEncryption() {
        new Expectations(encryptionService) {{
            domibusConfigurationService.isClusterDeployment();
            result = true;

            encryptionService.isAnyEncryptionActive();
            result = true;
        }};

        assertTrue(encryptionService.useLockForEncryption());
    }

    @Test
    public void useLockForEncryptionNoCluster() {
        new Expectations(encryptionService) {{
            domibusConfigurationService.isClusterDeployment();
            result = false;

            encryptionService.isAnyEncryptionActive();
            result = true;
        }};

        assertFalse(encryptionService.useLockForEncryption());
    }

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
            encryptionService.doHandleEncryption();
        }};

        encryptionService.handleEncryption();

        new Verifications() {{
            encryptionService.doHandleEncryption();
            times = 1;
        }};
    }

    @Test
    public void handleEncryptionWithLockFile(@Injectable File fileLock) {
        new Expectations(encryptionService) {{
            encryptionService.useLockForEncryption();
            result = true;

            encryptionService.getLockFileLocation();
            result = fileLock;
        }};

        encryptionService.handleEncryption();

        new Verifications() {{
            domainTaskExecutor.submit((Runnable) any, null, fileLock);
            times = 1;
        }};
    }

    @Test
    public void getLockFile() {
        String configLocation = "home";

        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = configLocation;
        }};

        final File lockFile = encryptionService.getLockFileLocation();
        assertEquals(configLocation, lockFile.getParent());
        assertEquals(EncryptionServiceImpl.ENCRYPTION_LOCK, lockFile.getName());
    }


}