package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.ebms3.sender.DispatchClientDefaultProvider.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of dispatcher related properties
 */
@Service
public class DispatchClientChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DISPATCHER_CONNECTIONTIMEOUT)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DISPATCHER_RECEIVETIMEOUT)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DISPATCHER_ALLOWCHUNKING)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DISPATCHER_CHUNKINGTHRESHOLD);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.DISPATCH_CLIENT);
    }
}
