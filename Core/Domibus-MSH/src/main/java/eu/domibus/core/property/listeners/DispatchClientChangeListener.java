package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.cache.DomibusLocalCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of dispatcher related properties
 */
@Service
public class DispatchClientChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomibusLocalCacheService domibusLocalCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_DISPATCHER_CONNECTIONTIMEOUT,
                DOMIBUS_DISPATCHER_RECEIVETIMEOUT,
                DOMIBUS_DISPATCHER_ALLOWCHUNKING,
                DOMIBUS_DISPATCHER_CHUNKINGTHRESHOLD);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusLocalCacheService.clearCache(DomibusLocalCacheService.DISPATCH_CLIENT);
    }
}
