package eu.domibus.ext.delegate.services.cache;

import eu.domibus.api.cache.CacheService;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class CacheServiceDelegateTest {

    @Tested
    CacheServiceDelegate cacheServiceDelegate;

    @Injectable
    CacheService cacheService;


    @Test
    public void evictCaches() {
        //tested method
        cacheServiceDelegate.evictCaches();

        new FullVerifications() {{
            cacheService.evictCaches();
        }};
    }

    @Test
    public void evictCachesWithException() {

        new Expectations() {{
            cacheService.evictCaches();
            result = new CacheExtServiceException(DomibusErrorCode.DOM_001, "exception");

        }};

        //tested method
        try {
            cacheServiceDelegate.evictCaches();
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof CacheExtServiceException);
        }
    }
}