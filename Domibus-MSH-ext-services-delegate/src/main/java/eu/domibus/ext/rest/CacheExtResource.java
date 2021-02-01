package eu.domibus.ext.rest;

import eu.domibus.ext.services.CacheExtService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Soumya Chandran
 * @since 4.1.1
 */
@RestController
@RequestMapping(value = "/ext/application")
public class CacheExtResource {

    @Autowired
    CacheExtService cacheExtService;

    /*@Autowired
    ExtExceptionHelper extExceptionHelper;

    @ExceptionHandler(PModeExtException.class)
    protected ResponseEntity<ErrorDTO> handlePModeExtException(PModeExtException e) {
        return extExceptionHelper.handleExtException(e);
    }*/

    @ApiOperation(value = "Delete all Caches",
            notes = "Clear all caches from the cacheManager",
            authorizations = @Authorization(value = "basicAuth"), tags = "cache")
    @DeleteMapping
    public void evictCaches() {

        cacheExtService.evictCaches();
    }
}
