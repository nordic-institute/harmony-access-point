package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.queue.FSSendMessageListenerContainer;
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
public class OutQueueConcurrencyChangeListener implements PluginPropertyChangeListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TriggerChangeListener.class);

    @Autowired
    private FSSendMessageListenerContainer messageListenerContainer;

    @Autowired
    protected DomainExtService domainExtService;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.endsWithIgnoreCase(propertyName, OUT_QUEUE_CONCURRENCY);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        DomainDTO domain = domainExtService.getDomain(domainCode);

        if (!fsPluginProperties.getDomainEnabled(domainCode)) {
            LOG.debug("Domain [{}] is disabled for FSPlugin exiting...", domainCode);
            return;
        }

        messageListenerContainer.updateMessageListenerContainerConcurrency(domain, propertyValue);
    }
}
