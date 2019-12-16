package eu.domibus.web.rest.testdb;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RawMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RawMessageService.class);

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    RawMessageDao rawMessageDao;

    public void updateRawMessage(RawMessage rawMessage, String type) {
        rawMessage.setRawEnvelope(rawMessage.getRawEnvelope().replace("HOLDER", "HOLDER" + type+System.currentTimeMillis()));
    }

    @Transactional
    public void saveSync(RawMessage rawMessage) {
        com.codahale.metrics.Timer.Context saveSyncTimer = metricRegistry.timer(MetricRegistry.name(TestDB.class, "saveSync")).time();
        rawMessageDao.create(rawMessage);
        saveSyncTimer.stop();
    }
}
