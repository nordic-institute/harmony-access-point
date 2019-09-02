package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.messaging.PluginMessageListenerContainer;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.OUT_QUEUE_CONCURRENCY;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of the fsplugin.send.queue.concurrency value.
 */
@Component
public class PluginMessageListenerConcurrencyChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private PluginMessageListenerContainer pluginMessageListenerContainer;

    @Autowired
    protected DomainExtService domainExtService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, OUT_QUEUE_CONCURRENCY);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        DomainDTO domain = domainExtService.getDomain(domainCode);
        pluginMessageListenerContainer.updateMessageListenerContainerConcurrency(domain, propertyValue);
    }
}
