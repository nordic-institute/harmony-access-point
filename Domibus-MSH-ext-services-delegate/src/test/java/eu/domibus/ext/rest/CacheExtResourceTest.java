package eu.domibus.ext.rest;

import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.CacheExtService;
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
public class CacheExtResourceTest {
    @Tested
    CacheExtResource cacheExtResource;

    @Injectable
    ExtExceptionHelper extExceptionHelper;

    @Injectable
    CacheExtService cacheExtService;

    @Test
    public void evictCaches() {
        //tested method
        cacheExtResource.evictCaches();

        new FullVerifications() {{
            cacheExtService.evictCaches();
        }};
    }

    @Test
    public void evictCachesWithException() {

        new Expectations() {{
            cacheExtService.evictCaches();
            result = new CacheExtServiceException(DomibusErrorCode.DOM_001, "exception");

        }};

        //tested method
        try {
            cacheExtResource.evictCaches();
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof CacheExtServiceException);
        }
    }
}