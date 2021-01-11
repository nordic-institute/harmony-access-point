package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public abstract class AbstractSignalLegConfigurationExtractor extends AbstractLegConfigurationExtractor {
    public AbstractSignalLegConfigurationExtractor(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    public LegConfiguration extractMessageConfiguration() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE_IN, MessageType.SIGNAL_MESSAGE);
        return process();
    }

    public abstract LegConfiguration process() throws EbMS3Exception;
}
