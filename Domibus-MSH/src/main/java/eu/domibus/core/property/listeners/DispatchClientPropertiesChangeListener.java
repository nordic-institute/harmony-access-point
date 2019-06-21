package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DispatchClientPropertiesChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.dispatcher.connectionTimeout")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dispatcher.receiveTimeout")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dispatcher.allowChunking")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dispatcher.chunkingThreshold");
        //
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.DISPATCH_CLIENT);
    }
}
