package eu.domibus.core.payload.encryption;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class DomibusEncryptionContextRefreshedListenerTest {

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
    DomibusEncryptionContextRefreshedListener domibusEncryptionContextRefreshedListener;


    @Test
    public void onApplicationEventThatShouldBeDiscarded(@Injectable ContextRefreshedEvent event,
                                                        @Injectable ApplicationContext applicationContext) {
        new Expectations() {{
            event.getApplicationContext();
            result = applicationContext;

            applicationContext.getParent();
            result = null;
        }};

        domibusEncryptionContextRefreshedListener.onApplicationEvent(event);

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

        domibusEncryptionContextRefreshedListener.onApplicationEvent(event);

        new Verifications() {{
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
            passwordEncryptionService.encryptPasswords();
        }};
    }

    @Test
    public void handleEncryption() {
        domibusEncryptionContextRefreshedListener.handleEncryption();

        new Verifications() {{
            encryptionService.createPayloadEncryptionKeyForAllDomainsIfNotExists();
            passwordEncryptionService.encryptPasswords();
        }};
    }


}