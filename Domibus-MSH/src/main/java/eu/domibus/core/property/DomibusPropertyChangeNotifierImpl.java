package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
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
public class DomibusPropertyChangeNotifierImpl implements DomibusPropertyChangeNotifier {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyChangeNotifierImpl.class);

    protected List<DomibusPropertyChangeListener> allPropertyChangeListeners;

    private SignalService signalService;

    public DomibusPropertyChangeNotifierImpl(
            List<DomibusPropertyChangeListener> propertyChangeListeners,
            List<PluginPropertyChangeListener> pluginPropertyChangeListeners,
            SignalService signalService) {

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
        List<DomibusPropertyChangeListener> listenersToNotify = allPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        LOG.trace("Notifying [{}] property change listeners of the change of [{}] property", listenersToNotify.size(), propertyName);
        listenersToNotify.forEach(listener -> notifyListener(listener, domainCode, propertyName, propertyValue));

        if (!broadcast) {
            LOG.trace("No broadcasting of property [{}] changed event", propertyName);
            return;
        }

        //signal for other nodes in the cluster
        LOG.trace("Broadcasting property [{}] changed event", propertyName);
        try {
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        } catch (Exception ex) {
            throw new DomibusPropertyException("Exception signaling property change for property " + propertyName, ex);
        }
    }

    protected void notifyListener(DomibusPropertyChangeListener listener, String domainCode, String propertyName, String propertyValue) {
        try {
            listener.propertyValueChanged(domainCode, propertyName, propertyValue);
        } catch (DomibusPropertyException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomibusPropertyException("Exception executing listener " + listener.getClass().getName() + " for property " + propertyName, ex);
        }
    }

    protected List<DomibusPropertyChangeListener> getPluginPropertyChangeListenerAdapters(List<PluginPropertyChangeListener> pluginPropertyChangeListeners) {
        List<DomibusPropertyChangeListener> result = new ArrayList<>();
        pluginPropertyChangeListeners.forEach(listener -> result.add(new PluginPropertyChangeListenerAdapter(listener)));
        return result;
    }
}
