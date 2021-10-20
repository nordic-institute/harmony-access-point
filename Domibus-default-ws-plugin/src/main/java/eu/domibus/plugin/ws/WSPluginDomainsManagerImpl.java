package eu.domibus.plugin.ws;

import eu.domibus.ext.services.DefaultDomainsAwareExt;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 *
 * Service responsible with reacting to adding and removing of domains at runtime
 */
@Service
public class WSPluginDomainsManagerImpl extends DefaultDomainsAwareExt {
    public WSPluginDomainsManagerImpl(WSPluginPropertyManager jmsPluginPropertyManager) {
        super(jmsPluginPropertyManager);
    }
}
