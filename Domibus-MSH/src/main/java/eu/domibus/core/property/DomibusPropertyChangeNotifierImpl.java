package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of a domibus property: notifies all listeners for custom actions and broadcasts to all nodes in the cluster
 */
@Service
public class DomibusPropertyChangeNotifierImpl implements DomibusPropertyChangeNotifier {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyChangeNotifierImpl.class);

    @Autowired
    private List<DomibusPropertyChangeListener> propertyChangeListeners;

    @Autowired
    private SignalService signalService;

    @Override
    public void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        //notify interested listeners that the property changed
        List<DomibusPropertyChangeListener> listeners = propertyChangeListeners.stream()
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
