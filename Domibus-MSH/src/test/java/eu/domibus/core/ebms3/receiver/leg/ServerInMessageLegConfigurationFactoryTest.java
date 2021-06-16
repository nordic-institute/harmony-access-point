package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.core.message.pull.PullRequestLegConfigurationFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@RunWith(MockitoJUnitRunner.class)
public class ServerInMessageLegConfigurationFactoryTest {

    @Mock
    private UserMessageLegConfigurationFactory userMessageLegConfigurationFactory;
    @Mock
    private PullRequestLegConfigurationFactory pullRequestLegConfigurationFactory;
    @Mock
    private ServerInReceiptLegConfigurationFactory serverInReceiptLegConfigurationFactory;
    @InjectMocks
    private ServerInMessageLegConfigurationFactory configurationFactory;

    @Test
    public void extractUserMessageConfiguration() {
        Ebms3Messaging ebms3Messaging = new Ebms3Messaging();
        ebms3Messaging.setUserMessage(new Ebms3UserMessage());
        SoapMessage soapMessage = new SoapMessage(new MessageImpl());
        configurationFactory.extractMessageConfiguration(soapMessage, ebms3Messaging);
        verify(userMessageLegConfigurationFactory, times(1)).
                extractMessageConfiguration(Mockito.eq(soapMessage), Mockito.eq(ebms3Messaging));
    }

}