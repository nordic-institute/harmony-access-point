package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.proxy.DomibusProxyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of proxy properties
 */
@Service
public class ProxyChangeListener implements DomibusPropertyChangeListener {

    private DomibusProxyService domibusProxyService;

    private DomibusCacheService domibusCacheService;

    public ProxyChangeListener(DomibusProxyService domibusProxyService, DomibusCacheService domibusCacheService) {
        this.domibusProxyService = domibusProxyService;
        this.domibusCacheService = domibusCacheService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_PROXY_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        domibusProxyService.resetProxy();
        domibusCacheService.clearCache(DomibusCacheService.DISPATCH_CLIENT);
    }

}
