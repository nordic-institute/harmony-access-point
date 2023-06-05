package eu.domibus.core.util;

import org.springframework.stereotype.Service;

import javax.persistence.TypedQuery;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class QueryUtil {

    /**
     * Set pagination values to query
     */
    public <T> void setPaginationParametersToQuery(TypedQuery<T> query, Integer pageStart, Integer pageSize) {

        // if page is not set start with the fist page
        int iMaxResults = pageSize == null || pageSize < 0 ? 0 : pageSize;
        int startingAt = (pageStart == null || pageStart < 0 ? 0 : pageStart) * iMaxResults;
        if (startingAt > 0) {
            query.setFirstResult(startingAt);
        }
        if (iMaxResults > 0) {
            query.setMaxResults(iMaxResults);
        }
    }
}
