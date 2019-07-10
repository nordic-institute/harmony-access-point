package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.DomibusPropertyChangeListener;
import eu.domibus.property.DomibusPropertyChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomibusPropertyChangeNotifierImpl implements DomibusPropertyChangeNotifier {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyChangeNotifierImpl.class);

    @Autowired
    private List<DomibusPropertyChangeListener> domibusPropertyChangeListeners;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        //notify interested listeners that the property changed
        List<DomibusPropertyChangeListener> listeners = domibusPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (Throwable ex) {
                LOGGER.error("An error occurred on setting property [{}] : [{}]", propertyName, ex);
            }
        });

        //signal for other nodes in the cluster
        if (broadcast) {
            SignalService signalService = applicationContext.getBean(SignalService.class);
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        }
    }
}
