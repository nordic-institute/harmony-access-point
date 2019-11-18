package eu.domibus.sti;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MapMessage;


public class JmsListener {

    private static final Logger LOG = LoggerFactory.getLogger(JmsListener.class);

    private SenderService senderService;

    private MetricRegistry metricRegistry;

    public JmsListener(SenderService senderService,
                       MetricRegistry metricRegistry) {
        this.senderService = senderService;
        this.metricRegistry = metricRegistry;
    }

    @org.springframework.jms.annotation.JmsListener(containerFactory = "myFactory", destination = "${jms.destinationName}")
    public void receiveMessage(MapMessage msj) {
        Timer.Context timer = null;
        try {
            timer = metricRegistry.timer(MetricRegistry.name(SenderService.class, "receive message")).time();
            LOG.debug("message receipt:[{}]", msj);
            senderService.reverseAndSend(msj);

        } finally {
            if (timer != null) {
                timer.stop();
            }
        }
    }
}
