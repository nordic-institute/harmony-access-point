package eu.domibus.performancetest;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.performancetest.PerformanceTestService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PerformanceTestServiceImpl implements PerformanceTestService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PerformanceTestServiceImpl.class);

    @Autowired
    protected RawMessageService rawMessageService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Override
    public void testSave(String type, int count) {
        byte[] payload = null;
        try {
            payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("50K_payload_SendMessage.xml"), "UTF-8").getBytes();
        } catch (IOException exc) {
            LOG.error("Exception ", exc);
        }

        List<RawMessage> rawMessages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RawMessage rawMessage = new RawMessage("[" + i + "]" + type + System.currentTimeMillis());
            rawMessage.setMessageId(i);
            rawMessage.setRawPayload(payload);
            rawMessages.add(rawMessage);
        }

        rawMessages.forEach(el -> {
            if (type.equals("async")) {
                //LOG.debug("Saving async [{}]", rawMessages.indexOf(el));
                saveAsync(el);
                //LOG.debug("Saved async [{}]", rawMessages.indexOf(el));
            } else {
                //LOG.info("Saving sync [{}]", rawMessages.indexOf(el));
                saveSync(el);
                //LOG.info("Saved sync [{}]", rawMessages.indexOf(el));
            }
        });
    }

    public void saveAsync(RawMessage rawMessage) {
        LOG.warn("Saving async [{}]", rawMessage.getMessageId());
        domainTaskExecutor.submit(() -> rawMessageService.saveSync(rawMessage, "SaveAsync-2"));
    }

    public void saveSync(RawMessage rawMessage) {
        rawMessageService.saveSync(rawMessage, "SaveSync-1");
    }
}
