package eu.domibus.core.spring;

import eu.domibus.api.encryption.EncryptionService;
import eu.domibus.core.plugin.routing.RoutingService;
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
public class DomibusContextRefreshedListenerTest {

    @Injectable
    protected EncryptionService encryptionService;

    @Injectable
    RoutingService routingService;

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
            encryptionService.handleEncryption();
            times = 0;
            routingService.init();
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
            routingService.init();
            times = 1;
        }};
    }


}