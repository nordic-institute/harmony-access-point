package eu.domibus.ext.rest;

import eu.domibus.ext.domain.CacheEntryDTO;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.DistributedCacheExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rest resource for managing the distributed cache
 *
 * @author Cosmin Baciu
 * @since 5.1
 */
@RestController
@RequestMapping(value = "/ext/distributed-cache")
@Tag(name = "cache", description = "Domibus Distributed Cache service")
@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_AP_ADMIN')")
public class DistributedCacheExtResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DistributedCacheExtResource.class);

    private final DistributedCacheExtService distributedCacheExtService;

    private final ExtExceptionHelper extExceptionHelper;

    public DistributedCacheExtResource(DistributedCacheExtService distributedCacheExtService, ExtExceptionHelper extExceptionHelper) {
        this.distributedCacheExtService = distributedCacheExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(CacheExtServiceException.class)
    protected ResponseEntity<ErrorDTO> handleCacheExtServiceException(CacheExtServiceException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Create distributed cache",
            description = "Create distributed cache using the specified configuration",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @PostMapping(path = "/caches")
    public void createDistributedCache(@RequestBody DistributedCacheCreateRequestDto createRequestDto) {
        LOG.info("Creating distributing cache");
        distributedCacheExtService.createCache(createRequestDto.getCacheName(), createRequestDto.getCacheSize(), createRequestDto.getTimeToLiveSeconds(), createRequestDto.getMaxIdleSeconds(), createRequestDto.getNearCacheSize(), createRequestDto.getNearCacheTimeToLiveSeconds(), createRequestDto.getNearCacheMaxIdleSeconds());
    }

    @Operation(summary = "Get all distributed cache names",
            description = "Get all distributed cache names",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @GetMapping(path = "/caches/names")
    public List<String> getDistributedCacheNames() {
        LOG.info("Getting all distributed cache names");

        return distributedCacheExtService.getDistributedCacheNames();
    }

    @Operation(summary = "Get an entry from the distributed cache",
            description = "Get an entry from the distributed cache",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @GetMapping(path = "/caches/{cacheName}/{entryKey}")
    public Object getDistributedCacheEntry(@PathVariable(name = "cacheName") String cacheName,
                                           @PathVariable(name = "entryKey") String entryKey) {
        LOG.info("Getting entry key [{}] from cache [{}]", entryKey, cacheName);

        return distributedCacheExtService.getEntryFromCache(cacheName, entryKey);
    }

    @Operation(summary = "Add an entry in the distributed cache",
            description = "Add an entry in the distributed cache",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @PostMapping(path = "/caches/{cacheName}")
    public void addDistributedCacheEntry(@PathVariable(name = "cacheName") String cacheName,
                                            @RequestBody CacheEntryDTO cacheEntry) {
        LOG.info("Creating entry in cache [{}]: [{}]", cacheName, cacheEntry);

        distributedCacheExtService.addEntry(cacheName, cacheEntry.getKey(), cacheEntry.getValue());
    }

    @Operation(summary = "Delete an entry from the distributed cache",
            description = "Delete an entry from the distributed cache",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @DeleteMapping(path = "/caches/{cacheName}/{entryKey}")
    public void deleteDistributedCacheEntry(@PathVariable(name = "cacheName") String cacheName,
                                            @PathVariable(name = "entryKey") String entryKey) {
        LOG.info("Deleting entry key [{}] from cache [{}]", entryKey, cacheName);

        distributedCacheExtService.evictEntryFromCache(cacheName, entryKey);
    }

    @Operation(summary = "Get all entries from a distributed cache",
            description = "Get all entries from a distributed cache",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin role needed")
    })
    @GetMapping(path = "/caches/{cacheName}")
    public List<CacheEntryDTO> getDistributedCacheEntries(@PathVariable(name = "cacheName") String cacheName) {
        LOG.info("Getting all entries from cache [{}]", cacheName);

        return distributedCacheExtService.getEntriesFromCache(cacheName);
    }
}
