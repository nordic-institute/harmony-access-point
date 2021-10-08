package eu.domibus.core.spring;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.core.message.dictionary.StaticDictionaryService;
import eu.domibus.core.plugin.routing.BackendFilterInitializerService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;
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
    protected StaticDictionaryService messageDictionaryService;

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

        assertTrue(domibusContextRefreshedListener.useLockForExecution());
    }

    @Test
    public void useLockForEncryptionNoCluster() {
        new Expectations() {{
            domibusConfigurationService.isClusterDeployment();
            result = false;
        }};

        assertFalse(domibusContextRefreshedListener.useLockForExecution());
    }

    @Test
    public void handleEncryptionWithLockFile(@Injectable File fileLock, @Injectable Runnable task) {
        new Expectations(domibusContextRefreshedListener) {{
            domibusContextRefreshedListener.useLockForExecution();
            result = true;
        }};

        domibusContextRefreshedListener.executeWithLockIfNeeded(task);

        new Verifications() {{
            domainTaskExecutor.submit(task, (Runnable) any, SYNC_LOCK_KEY, true, 3L, TimeUnit.MINUTES);
            times = 1;
        }};
    }

}
