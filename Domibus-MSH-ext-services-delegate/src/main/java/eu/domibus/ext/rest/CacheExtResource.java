package eu.domibus.ext.rest;

import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.CacheExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(value = "/ext")
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

    @ApiOperation(value = "[ADMIN] Evict all Caches",
            notes = "Clear all caches from the cacheManager (Admin rights needed)",
            authorizations = @Authorization(value = "basicAuth"), tags = "cache")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    @ApiResponses({
            @ApiResponse(code=403, message = "Admin role needed")
    })
    @DeleteMapping(path = "/cache")
    public void evictCaches() {
        LOG.info("External API call to Clear all caches..");
        cacheExtService.evictCaches();
    }

    @ApiOperation(value = "[ADMIN] Evict Second Level Caches",
            notes = "[ADMIN] Clear second level caches including query caches (Admin rights needed)",
            authorizations = @Authorization(value = "basicAuth"), tags = "cache")
    @DeleteMapping(path = "/2LCache")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
    @ApiResponses({
            @ApiResponse(code=403, message = "Admin role needed")
    })
    public void evictTechnicalCaches() {
        LOG.info("External API call to Clear Second Level Caches and Query Caches..");
        cacheExtService.evict2LCaches();
    }
}
