package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
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
public class CacheDefaultServiceTest {
    @Tested
    CacheDefaultService cacheDefaultService;

    @Injectable
    DomibusCacheService domibusCacheService;


    @Test
    public void evictCaches() {
        //tested method
        cacheDefaultService.evictCaches();

        new FullVerifications() {{
            domibusCacheService.clearAllCaches();
        }};
    }

    @Test
    public void evictCachesWithException() {

        new Expectations() {{
            domibusCacheService.clearAllCaches();
            result = new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "exception");

        }};

        //tested method
        try {
            cacheDefaultService.evictCaches();
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof DomibusCoreException);
        }
    }


}