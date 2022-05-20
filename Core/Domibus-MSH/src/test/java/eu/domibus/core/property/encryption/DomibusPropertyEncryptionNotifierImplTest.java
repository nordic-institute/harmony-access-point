package eu.domibus.core.property.encryption;

import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class DomibusPropertyEncryptionNotifierImplTest {

    @Tested
    protected DomibusPropertyEncryptionNotifierImpl domibusPropertyEncryptionNotifier;

    @Test
    public void signalEncryptPasswords(@Mocked PluginPropertyEncryptionListener pluginPropertyEncryptionListener) {
        domibusPropertyEncryptionNotifier.signalEncryptPasswords();

        new Verifications() {{
            pluginPropertyEncryptionListener.encryptPasswords();
            times = 0;
        }};
    }
}