package eu.domibus.core.spring;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class DomibusContextRefreshedListenerTest {

    @Tested
    DomibusContextRefreshedListener domibusContextRefreshedListener;

    @Injectable
    protected BackendFilterInitializerService backendFilterInitializerService;

    @Injectable
    protected EncryptionService encryptionService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

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
            encryptionService.handleEncryption();
            times = 0;

            backendFilterInitializerService.updateMessageFilters();
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
            encryptionService.handleEncryption();
            times = 1;

            backendFilterInitializerService.updateMessageFilters();
            times = 1;
        }};
    }

    @Test
    public void useLockForEncryption() {
        new Expectations() {{
            domibusConfigurationService.isClusterDeployment();
            result = true;
        }};

        assertTrue(domibusContextRefreshedListener.isClusterEnvironment());
    }

    @Test
    public void useLockForEncryptionNoCluster() {
        new Expectations() {{
            domibusConfigurationService.isClusterDeployment();
            result = false;
        }};

        assertFalse(domibusContextRefreshedListener.isClusterEnvironment());
    }

    @Test
    public void handleEncryptionWithLockFile(@Injectable File fileLock, @Injectable Runnable task) {
        new Expectations(domibusContextRefreshedListener) {{
            domibusContextRefreshedListener.isClusterEnvironment();
            result = true;

            domibusContextRefreshedListener.getLockFileLocation();
            result = fileLock;
        }};

        domibusContextRefreshedListener.executeWithLockIfNeeded(task);

        new Verifications() {{
            domainTaskExecutor.submit(task, null, fileLock);
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

        final File lockFile = domibusContextRefreshedListener.getLockFileLocation();

        assertEquals(configLocation, lockFile.getParent());
        assertEquals(DomibusContextRefreshedListener.ENCRYPTION_LOCK, lockFile.getName());
    }
}