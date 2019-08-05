package eu.domibus.ext.delegate.converter;

import java.util.List;
import java.util.Map;

/**
 * Class responsible of conversion from the internal domain to external domain and the other way around
 *
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface DomainExtConverter {

    <T, U> T convert(U source, Class<T> typeOfT);

    <T, U> List<T> convert(List<U> sourceList, Class<T> typeOfT);

    <T, U> Map<String, T> convert(Map<String, U> source, Class<T> typeOfT);
}
