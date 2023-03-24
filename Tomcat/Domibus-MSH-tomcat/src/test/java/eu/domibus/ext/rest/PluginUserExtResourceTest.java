package eu.domibus.ext.rest;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PluginUserExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PluginUserExtResourceTest {

    @Tested
    PluginUserExtResource pluginUserExtResource;

    @Injectable
    PluginUserExtService pluginUserExtService;

    @Injectable
    ExtExceptionHelper extExceptionHelper;

    @Test
    public void testCreatePluginUser(@Mocked PluginUserDTO pluginUserDTO) {
        String userName = "testUserName";

        new Expectations(){{
            pluginUserDTO.getUserName();
            result = userName;
        }};

        pluginUserExtResource.createPluginUser(pluginUserDTO);

        new FullVerifications(){{
            pluginUserExtService.createPluginUser(pluginUserDTO);
        }};
    }

    @Test(expected = PluginUserExtServiceException.class)
    public void testCreatePluginUserErrorHandler(@Mocked PluginUserDTO pluginUserDTO) {
        DomibusCoreException domibusCoreException = new DomibusCoreException("Test error message.");
        new Expectations(){{
            pluginUserExtService.createPluginUser(pluginUserDTO);
            result = domibusCoreException;
        }};

        pluginUserExtResource.createPluginUser(pluginUserDTO);
    }
}