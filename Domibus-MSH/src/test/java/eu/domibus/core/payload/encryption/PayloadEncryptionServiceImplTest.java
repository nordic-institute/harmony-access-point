package eu.domibus.core.payload.encryption;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.encryption.EncryptionKeyDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class PayloadEncryptionServiceImplTest {

    @Injectable
    protected EncryptionKeyDao encryptionKeyDao;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected EncryptionUtil encryptionUtil;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Tested
    PayloadEncryptionServiceImpl payloadEncryptionService;

    @Test
    public void useLockForEncryption() {
        new Expectations(payloadEncryptionService) {{
            domibusConfigurationService.isClusterDeployment();
            result = true;

            payloadEncryptionService.isAnyEncryptionActive();
            result = true;
        }};

        assertTrue(payloadEncryptionService.useLockForEncryption());
    }

    @Test
    public void useLockForEncryptionNoCluster() {
        new Expectations(payloadEncryptionService) {{
            domibusConfigurationService.isClusterDeployment();
            result = false;

            payloadEncryptionService.isAnyEncryptionActive();
            result = true;
        }};

        assertFalse(payloadEncryptionService.useLockForEncryption());
    }

    @Test
    public void isAnyEncryptionActiveWithGeneralEncryptionActive() {
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive();
            result = true;
        }};

        assertTrue(payloadEncryptionService.isAnyEncryptionActive());
    }

    @Test
    public void getLockFile() {
        String configLocation  = "home";

        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = configLocation;
        }};

        final File lockFile = payloadEncryptionService.getLockFileLocation();
        assertEquals(configLocation, lockFile.getParent());
        assertEquals(PayloadEncryptionServiceImpl.ENCRYPTION_LOCK, lockFile.getName());
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

        assertTrue(payloadEncryptionService.isAnyEncryptionActive());
    }

}