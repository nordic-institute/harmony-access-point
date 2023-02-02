package eu.domibus.core.util;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept(T t, U u, V v);
}
