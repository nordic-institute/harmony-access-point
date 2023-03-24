package eu.domibus.ext.services;

import eu.domibus.ext.domain.PluginUserDTO;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;

/**
 * External service for Plugin Users management
 *
 * @author Arun Raj
 * @since 5.0
 */
public interface PluginUserExtService {

    /**
     * creates a PluginUser with role either User or Admin
     *
     * @param pluginUserDTO
     */
    void createPluginUser(final PluginUserDTO pluginUserDTO) throws PluginUserExtServiceException;

}
