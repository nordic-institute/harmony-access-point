package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("clientInMessageLegConfigurationFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClientInMessageLegConfigurationFactory implements MessageLegConfigurationFactory {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClientInMessageLegConfigurationFactory.class);

    @Autowired
    private UserMessageLegConfigurationFactory userMessageLegConfigurationFactory;

    @Autowired
    private ClientInReceiptLegConfigurationFactory clientInReceiptLegConfigurationFactory;

    @Autowired
    private ErrorSignalConfigurationFactory errorSignalConfigurationFactory;


    @PostConstruct
    void init() {
        userMessageLegConfigurationFactory.
                chain(clientInReceiptLegConfigurationFactory).
                chain(errorSignalConfigurationFactory);

    }

    @Override
    public LegConfigurationExtractor extractMessageConfiguration(SoapMessage soapMessage, Messaging messaging) {
        LegConfigurationExtractor legConfigurationExtractor = userMessageLegConfigurationFactory.extractMessageConfiguration(soapMessage, messaging);
        if (legConfigurationExtractor == null) {
            LOG.error("Leconfiguration not found for incoming message with id " + messaging.getId());
        }
        return legConfigurationExtractor;
    }
}
