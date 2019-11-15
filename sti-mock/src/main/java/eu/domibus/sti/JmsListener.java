package eu.domibus.sti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MapMessage;


public class JmsListener {

    private static final Logger LOG = LoggerFactory.getLogger(JmsListener.class);

    private SenderService senderService;

    public JmsListener(SenderService senderService) {
        this.senderService = senderService;
    }

    @org.springframework.jms.annotation.JmsListener(containerFactory = "myFactory", destination = "${jms.destinationName}")
    public void receiveMessage(MapMessage msj) {
        LOG.debug("message receipt:[{}]",msj);
        senderService.reverseAndSend(msj);
    }
}
