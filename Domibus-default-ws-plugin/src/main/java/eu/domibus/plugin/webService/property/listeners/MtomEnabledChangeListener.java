package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.MTOM_ENABLED_PROPERTY;

/**
 * Handles the change of wsplugin.mtom.enabled property of backendInterfaceEndpoint
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class MtomEnabledChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private Endpoint backendInterfaceEndpoint;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equals(propertyName, MTOM_ENABLED_PROPERTY);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        Boolean flag = Boolean.parseBoolean(propertyValue);
        ((SOAPBinding) backendInterfaceEndpoint.getBinding()).setMTOMEnabled(flag);
    }
}
