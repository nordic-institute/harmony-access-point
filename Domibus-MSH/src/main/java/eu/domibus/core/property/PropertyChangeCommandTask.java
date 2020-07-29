package eu.domibus.core.property;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PropertyChangeCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(PropertyChangeCommandTask.class);

    private DomibusPropertyProvider domibusPropertyProvider;
    protected DomainContextProvider domainContextProvider;

    public PropertyChangeCommandTask(DomibusPropertyProvider domibusPropertyProvider, DomainContextProvider domainContextProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.DOMIBUS_PROPERTY_CHANGE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Property change level command");

        final String propName = properties.get(CommandProperty.PROPERTY_NAME);
        final String propVal = properties.get(CommandProperty.PROPERTY_VALUE);

        Domain domain = domainContextProvider.getCurrentDomain();
        try {
            LOGGER.trace("Updating the value of [{}] property on domain [{}], no broadcast", propName, domain);
            domibusPropertyProvider.setProperty(domain, propName, propVal, false);
        } catch (Exception ex) {
            LOGGER.error("Error trying to set property [{}] with value [{}] on domain [{}]", propName, propVal, domain);
        }
    }
}
