package eu.domibus.core.message;

import eu.domibus.core.message.MessageExchangeConfiguration;
import org.junit.Test;

import static eu.domibus.core.message.MessageExchangeConfiguration.PMODEKEY_SEPARATOR;
import static org.junit.Assert.assertEquals;
/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
public class MessageExchangeConfigurationTest {

    @Test
    public void testMessageExchange(){
        //final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg,String pmodeKey) {
        String agreementName="agreementName";
        String senderParty="senderParty";
        String receiverParty="receiverParty";
        String service="service";
        String action="action";
        String leg="leg";

        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(agreementName, senderParty, receiverParty, service, action, leg);
        assertEquals(agreementName, messageExchangeConfiguration.getAgreementName());
        assertEquals(senderParty, messageExchangeConfiguration.getSenderParty());
        assertEquals(receiverParty, messageExchangeConfiguration.getReceiverParty());
        assertEquals(service, messageExchangeConfiguration.getService());
        assertEquals(action, messageExchangeConfiguration.getAction());
        assertEquals(leg, messageExchangeConfiguration.getLeg());
        assertEquals(senderParty+PMODEKEY_SEPARATOR+receiverParty+PMODEKEY_SEPARATOR+service+PMODEKEY_SEPARATOR+action+PMODEKEY_SEPARATOR+agreementName+PMODEKEY_SEPARATOR+leg, messageExchangeConfiguration.getPmodeKey());
    }

}