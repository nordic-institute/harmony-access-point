package eu.domibus.core.property.encryption;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class DomibusPropertyEncryptionNotifierImpl implements DomibusPropertyEncryptionNotifier {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyEncryptionNotifierImpl.class);

    @Autowired(required = false)
    protected List<PluginPropertyEncryptionListener> pluginPropertyEncryptionListeners;

    public void signalEncryptPasswords() {
        if (pluginPropertyEncryptionListeners == null) {
            LOG.debug("No pluginPropertyEncryptionListeners registered");
            return;
        }

        for (PluginPropertyEncryptionListener pluginPropertyEncryptionListener : pluginPropertyEncryptionListeners) {
            try {
                LOG.debug("Found pluginPropertyEncryptionListeners registered: [{}] ", pluginPropertyEncryptionListener);
                pluginPropertyEncryptionListener.encryptPasswords();
            } catch (Exception e) {
                LOG.error("Error while notifying pluginPropertyEncryptionListeners", e);
            }

        }
    }
}
