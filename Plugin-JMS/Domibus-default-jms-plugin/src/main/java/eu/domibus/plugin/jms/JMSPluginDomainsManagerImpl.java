package eu.domibus.plugin.jms;

import eu.domibus.ext.services.DefaultDomainsAwareExt;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Service responsible with reacting to adding and removing of domains at runtime
 */
@Service
public class JMSPluginDomainsManagerImpl extends DefaultDomainsAwareExt {

    public JMSPluginDomainsManagerImpl(JmsPluginPropertyManager jmsPluginPropertyManager) {
        super(jmsPluginPropertyManager);
    }

}
