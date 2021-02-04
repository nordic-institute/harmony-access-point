package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.CacheExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * External API for clearing caches
 *
 * @author Soumya Chandran
 * @since 5.0
 */
@RestController
@RequestMapping(value = "/ext/cache")
public class CacheExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CacheExtResource.class);

    private final CacheExtService cacheExtService;

    private final ExtExceptionHelper extExceptionHelper;

    public CacheExtResource(CacheExtService cacheExtService, ExtExceptionHelper extExceptionHelper) {
        this.cacheExtService = cacheExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(CacheExtServiceException.class)
    protected ResponseEntity<ErrorDTO> handleCacheExtServiceException(CacheExtServiceException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @ApiOperation(value = "Delete all Caches",
            notes = "Clear all caches from the cacheManager",
            authorizations = @Authorization(value = "basicAuth"), tags = "cache")
    @DeleteMapping
    public void evictCaches() {
        LOG.info("External API call to Clear all caches..");
        cacheExtService.evictCaches();
    }
}
