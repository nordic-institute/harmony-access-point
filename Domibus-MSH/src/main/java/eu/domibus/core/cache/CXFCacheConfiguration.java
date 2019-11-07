package eu.domibus.core.cache;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.rt.security.utils.SecurityUtils;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.EHCacheTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
@Configuration
public class CXFCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CXFCacheConfiguration.class);

    @Bean("ehCacheTokenStore")
    public EHCacheTokenStore ehCacheTokenStore(@Autowired Bus bus) {
        final ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
        final URL url = SecurityUtils.loadResource(resourceManager, "cxf-ehcache.xml");
        LOG.debug("Loading the CXF EHCacheTokenStore from [{}]", url);
        String cacheKey = SecurityConstants.TOKEN_STORE_CACHE_INSTANCE;
        EHCacheTokenStore ehCacheManager = new EHCacheTokenStore(cacheKey, bus, url);
        return ehCacheManager;
    }
}
