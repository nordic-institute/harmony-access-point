package eu.domibus.core.ehcache;

import net.sf.ehcache.pool.sizeof.annotations.IgnoreSizeOf;

/**
 * Generic wrapper around any type of object which is complex and is used as return type from a {@code @Cacheable} method
 *
 * As the wrapped object have {@code IgnoreSizeOf} enabled the Ehcache will not calculate the size
 * @since 4.2
 * @author Catalin Enache
 * @param <T> object for which we add {@code IgnoreSizeOf} for ehcache
 */
public class IgnoreSizeOfWrapper<T> {

    @IgnoreSizeOf
    private T objectToWrap;

    public IgnoreSizeOfWrapper(T objectToWrap) {
        this.objectToWrap = objectToWrap;
    }

    public T get() {
        return objectToWrap;
    }
}
