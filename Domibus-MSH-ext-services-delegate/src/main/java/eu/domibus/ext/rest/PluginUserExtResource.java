package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.services.PluginUserExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/ext/pluginUser")
@Tag(name = "pluginUser", description = "Domibus Plugin User management API")
public class PluginUserExtResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyExtResource.class);

    @Autowired
    PluginUserExtService pluginUserExtService;


    @Operation(summary="Creates a Plugin User",
            description="Creates a Plugin User using username, original user, role and password.",
            security = @SecurityRequirement(name ="DomibusBasicAuth"))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createParty(@RequestBody PluginUserDTO request) {
        pluginUserExtService.createPluginUser(request);
        return "Plugin User having userName=[" + request.getUserName() + "] created successfully!";
    }
}
