package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public abstract class AbstractMessageLegConfigurationFactory implements MessageLegConfigurationFactory {

    private AbstractMessageLegConfigurationFactory next;

    private MessageLegConfigurationVisitor messageLegConfigurationVisitor;

    public AbstractMessageLegConfigurationFactory chain(AbstractMessageLegConfigurationFactory next) {
        this.next = next;
        return this.next;
    }

    public LegConfigurationExtractor extractMessageConfiguration(final SoapMessage soapMessage, final Ebms3Messaging ebms3Messaging) {
        LegConfigurationExtractor configuration = getConfiguration(soapMessage, ebms3Messaging);
        if (configuration == null) {
            configuration = executeNextFactory(soapMessage, ebms3Messaging);
        } else {
            //@thom remove the visitor, do the injection directly in the factory.
            configuration.accept(messageLegConfigurationVisitor);
        }
        return configuration;
    }

    abstract protected LegConfigurationExtractor getConfiguration(final SoapMessage soapMessage, final Ebms3Messaging ebms3Messaging);


    private LegConfigurationExtractor executeNextFactory(SoapMessage soapMessage, Ebms3Messaging messaging) {
        if (next != null) {
            return next.extractMessageConfiguration(soapMessage, messaging);
        } else {
            return null;
        }
    }

    @Autowired
    public void setMessageLegConfigurationVisitor(MessageLegConfigurationVisitor messageLegConfigurationVisitor) {
        this.messageLegConfigurationVisitor = messageLegConfigurationVisitor;
    }
}
