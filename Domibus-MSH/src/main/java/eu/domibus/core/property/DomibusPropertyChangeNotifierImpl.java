package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the change of a configuration property, being it a core or external module property that is handled globally(JMS< WS)
 * Notifies all listeners ( core and adapted external module listeners ) and broadcasts to all nodes in the cluster
 *
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class DomibusPropertyChangeNotifierImpl implements DomibusPropertyChangeNotifier, PluginPropertyChangeNotifier {

    protected List<DomibusPropertyChangeListener> allPropertyChangeListeners;

    private SignalService signalService;

    public DomibusPropertyChangeNotifierImpl(
            @Autowired List<DomibusPropertyChangeListener> propertyChangeListeners,
            @Autowired(required = false) List<PluginPropertyChangeListener> pluginPropertyChangeListeners,
            @Autowired SignalService signalService) {

        this.signalService = signalService;

        allPropertyChangeListeners = new ArrayList<>();
        allPropertyChangeListeners.addAll(propertyChangeListeners);

        //adapt plugin prop change listeners to treat them all polimorphically
        List<DomibusPropertyChangeListener> pluginPropertyChangeListenerAdapters = getPluginPropertyChangeListenerAdapters(pluginPropertyChangeListeners);
        allPropertyChangeListeners.addAll(pluginPropertyChangeListenerAdapters);
    }

    @Override
    public void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast)
            throws DomibusPropertyException {

        //notify all interested listeners(core or external) that the property changed
        List<DomibusPropertyChangeListener> listeners = allPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (DomibusPropertyException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new DomibusPropertyException("Exception executing listener " + listener.getClass().getName() + " for property " + propertyName, ex);
            }
        });

        if (!broadcast) {
            return;
        }

        //signal for other nodes in the cluster
        try {
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        } catch (Exception ex) {
            throw new DomibusPropertyException("Exception signaling property change for property " + propertyName, ex);
        }
    }

    protected List<DomibusPropertyChangeListener> getPluginPropertyChangeListenerAdapters(List<PluginPropertyChangeListener> pluginPropertyChangeListeners) {
        List<DomibusPropertyChangeListener> result = new ArrayList<>();
        pluginPropertyChangeListeners.forEach(listener -> result.add(new PluginPropertyChangeListenerAdapter(listener)));
        return result;
    }
}
