package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_SUPPORT_TEAM_NAME;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;
import static eu.domibus.property.ExternalTestModulePropertyManager.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class DomibusPropertyProviderIT extends AbstractIT {

    @Autowired
    org.springframework.cache.CacheManager cacheManager;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    Domain defaultDomain = new Domain("default", "Default");

    @Test
    public void testCache() {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        //not in cache now
        String cachedValue = getCachedValue(defaultDomain, propertyName);
        //ads to cache
        String actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        Assert.assertNotEquals(actualValue, cachedValue);

        //gets the cached value now
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertEquals(actualValue, cachedValue);
    }

    @Test
    public void testCacheEvict() {
        String propertyName = DOMIBUS_UI_SUPPORT_TEAM_NAME;

        String cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNull(cachedValue);
        //ads to cache
        String actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        //gets the cached value now
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNotNull(cachedValue);

        String newValue = actualValue + "MODIFIED";
        //evicts from cache
        domibusPropertyProvider.setProperty(defaultDomain, propertyName, newValue);
        //so not in cache
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertNull(cachedValue);

        //ads to cache again
        actualValue = domibusPropertyProvider.getProperty(defaultDomain, propertyName);
        //finds it there
        cachedValue = getCachedValue(defaultDomain, propertyName);
        Assert.assertEquals(newValue, actualValue);
    }

    private String getCachedValue(Domain domain, String propertyName) {
        return cacheManager.getCache(DomibusCacheService.DOMIBUS_PROPERTY_CACHE).get(domain.getCode() + propertyName, String.class);
    }

    @Test
    public void getPropertyValue_non_existing() {
        String propName = EXTERNAL_NOT_EXISTENT;
        DomibusPropertyMetadata result = globalPropertyMetadataManager.getPropertyMetadata(propName);
        Assert.assertEquals(DomibusPropertyMetadata.Usage.ANY.getValue(), result.getUsage());
    }

    @Test
    public void getPropertyValue_existing_storedLocally_notHandled() {
        String propName = EXTERNAL_MODULE_EXISTENT_NOT_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = "true";

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(null, result);

        domibusPropertyProvider.setProperty(currentDomain, propName, propValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(null, result2);
    }

    @Test
    public void getPropertyValue_existing_storedLocally_handled() {
        String propName = EXTERNAL_MODULE_EXISTENT_LOCALLY_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = propName + ".value";

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(propValue, result);

        String newValue = "newValue";
        domibusPropertyProvider.setProperty(currentDomain, propName, newValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(newValue, result2);
    }

    @Test
    public void getPropertyValue_existing_storedGlobally() {
        String propName = EXTERNAL_MODULE_EXISTENT_GLOBALLY_HANDLED;
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        String propValue = null;

        String result = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(propValue, result);

        String newValue = "newValue";
        domibusPropertyProvider.setProperty(currentDomain, propName, newValue);
        String result2 = domibusPropertyProvider.getProperty(currentDomain, propName);
        Assert.assertEquals(newValue, result2);
    }
}
