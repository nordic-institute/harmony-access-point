package eu.domibus.core.crypto.spi.dss;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */

@RunWith(JMockit.class)
public class DssCacheTest {

    @Test
    public void addToCache(@Mocked Cache cache) {
        DssCache dssCache = new DssCache(cache);
        String key = "key";
        Boolean valid = true;
        dssCache.addToCache(key, valid);
        new Verifications(){{
            Element element;
            cache.putIfAbsent(element=withCapture());
            assertEquals(key,element.getObjectKey());
            assertEquals(valid,element.getObjectValue());
        }};
    }

    @Test
    public void isChainValid(@Mocked Cache cache,@Mocked Element element) {
        DssCache dssCache = new DssCache(cache);
        String key = "key";
        new Expectations(){{
           cache.get(key);
           returns(element,null);
        }};
        assertTrue(dssCache.isChainValid(key));
        assertFalse(dssCache.isChainValid(key));
    }

    @Test
    public void clear(@Mocked Cache cache) {
        DssCache dssCache = new DssCache(cache);
        dssCache.clear();
        new Verifications(){{
           cache.removeAll();
        }};
    }
}