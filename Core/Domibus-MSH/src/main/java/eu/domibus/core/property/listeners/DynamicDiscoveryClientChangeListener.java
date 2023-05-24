package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.pmode.provider.PModeProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of dynamicdiscovery client propertiy
 */
@Service
public class DynamicDiscoveryClientChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected PModeProvider pModeProvider;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        pModeProvider.refresh();
    }
}
