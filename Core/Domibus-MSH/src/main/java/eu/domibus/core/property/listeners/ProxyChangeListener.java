package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.proxy.DomibusProxyService;
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

    private DomibusLocalCacheService domibusLocalCacheService;

    public ProxyChangeListener(DomibusProxyService domibusProxyService, DomibusLocalCacheService domibusLocalCacheService) {
        this.domibusProxyService = domibusProxyService;
        this.domibusLocalCacheService = domibusLocalCacheService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_PROXY_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        domibusProxyService.resetProxy();
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.DISPATCH_CLIENT);
    }

}
