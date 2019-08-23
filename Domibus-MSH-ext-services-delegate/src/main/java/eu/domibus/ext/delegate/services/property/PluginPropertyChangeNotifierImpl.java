package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * The plugin api equivalent of DomibusPropertyChangeNotifierImpl
 * Handles the change of a domibus property: notifies all listeners for custom actions and broadcasts to all nodes in the cluster
 */
@Service
public class PluginPropertyChangeNotifierImpl implements PluginPropertyChangeNotifier {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(PluginPropertyChangeNotifierImpl.class);

    @Autowired
    private List<PluginPropertyChangeListener> pluginPropertyChangeListeners;

    @Autowired
    private SignalService signalService;

    @Override
    public void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        //notify interested listeners that the property changed
        List<PluginPropertyChangeListener> listeners = pluginPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (Throwable ex) {
                LOGGER.error("An error occurred on setting property [{}]", propertyName, ex);
            }
        });

        //signal for other nodes in the cluster
        if (broadcast) {
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        }
    }
}
