package eu.domibus.spring;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.PasswordEncryptionService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import mockit.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class DomibusContextRefreshedListenerTest {

    @Injectable
    protected PayloadEncryptionService encryptionService;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomainService domainService;

    @Tested
    DomibusContextRefreshedListener domibusContextRefreshedListener;


    @Test
    public void onApplicationEventThatShouldBeDiscarded(@Injectable ContextRefreshedEvent event,
                                                        @Injectable ApplicationContext applicationContext) {
        new Expectations() {{
            event.getApplicationContext();
            result = applicationContext;

            applicationContext.getParent();
            result = null;
        }};

        domibusContextRefreshedListener.onApplicationEvent(event);

        new Verifications() {{
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
            times = 0;
        }};
    }


    @Test
    public void onApplicationEvent(@Injectable ContextRefreshedEvent event,
                                   @Injectable ApplicationContext applicationContext,
                                   @Injectable ApplicationContext parent) {
        new Expectations() {{
            event.getApplicationContext();
            result = applicationContext;

            applicationContext.getParent();
            result = parent;
        }};

        domibusContextRefreshedListener.onApplicationEvent(event);

        new Verifications() {{
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
            passwordEncryptionService.encryptPasswords();
        }};
    }

    @Test
    public void useLockForEncryption() {
        new Expectations(domibusContextRefreshedListener) {{
            domibusConfigurationService.isClusterDeployment();
            result = true;

            domibusContextRefreshedListener.isAnyEncryptionActive();
            result = true;
        }};

        assertTrue(domibusContextRefreshedListener.useLockForEncryption());
    }

    @Test
    public void useLockForEncryptionNoCluster() {
        new Expectations(domibusContextRefreshedListener) {{
            domibusConfigurationService.isClusterDeployment();
            result = false;

            domibusContextRefreshedListener.isAnyEncryptionActive();
            result = true;
        }};

        assertFalse(domibusContextRefreshedListener.useLockForEncryption());
    }

    @Test
    public void isAnyEncryptionActiveWithGeneralEncryptionActive() {
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive();
            result = true;
        }};

        assertTrue(domibusContextRefreshedListener.isAnyEncryptionActive());
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

        assertTrue(domibusContextRefreshedListener.isAnyEncryptionActive());
    }

    @Test
    public void handleEncryption() {
        domibusContextRefreshedListener.handleEncryption();

        new Verifications() {{
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
            passwordEncryptionService.encryptPasswords();
        }};
    }

    @Test
    public void getLockFile() {
        String configLocation  = "home";

        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = configLocation;
        }};

        final File lockFile = domibusContextRefreshedListener.getLockFileLocation();
        assertEquals(configLocation, lockFile.getParent());
        assertEquals(DomibusContextRefreshedListener.ENCRYPTION_LOCK, lockFile.getName());


    }
}