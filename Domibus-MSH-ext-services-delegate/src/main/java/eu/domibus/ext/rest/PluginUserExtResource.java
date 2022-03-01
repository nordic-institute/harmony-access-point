package eu.domibus.ext.rest;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.ext.domain.ErrorDTO;
import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PluginUserExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/ext/pluginUser")
@Tag(name = "pluginUser", description = "Domibus Plugin User management API")
public class PluginUserExtResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserExtResource.class);

    protected PluginUserExtService pluginUserExtService;
    protected ExtExceptionHelper extExceptionHelper;

    public PluginUserExtResource(PluginUserExtService pluginUserExtService, ExtExceptionHelper extExceptionHelper) {
        this.pluginUserExtService = pluginUserExtService;
        this.extExceptionHelper = extExceptionHelper;
    }

    @ExceptionHandler(PluginUserExtServiceException.class)
    public ResponseEntity<ErrorDTO> handlePluginUserExtServiceException(PluginUserExtServiceException e) {
        return extExceptionHelper.handleExtException(e);
    }

    @Operation(summary = "Creates a Plugin User",
            description = "Creates a Plugin User using username, original user, role and password.",
            security = @SecurityRequirement(name = "DomibusBasicAuth"))
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public String createPluginUser(@RequestBody PluginUserDTO request) {
        try {
            pluginUserExtService.createPluginUser(request);
        } catch (DomibusCoreException e) {
            LOG.error("Encountered exception at create plugin user:", e);
            throw new PluginUserExtServiceException(e.getMessage(), e);
        }
        return "Plugin User having userName=[" + request.getUserName() + "] created successfully!";
    }
}
