package eu.domibus.performancetest;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.performancetest.PerformanceTestService;
import eu.domibus.common.NotificationType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.NotifyMessageCreator;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    protected MetricRegistry metricRegistry;

    @Autowired
    protected JMSManager jmsManager;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void testDBSaveAndJMS() {
        com.codahale.metrics.Timer.Context testDBSaveAndJMSInside = metricRegistry.timer(MetricRegistry.name(PerformanceTestServiceImpl.class, "testDBSaveAndJMSInside")).time();

        com.codahale.metrics.Timer.Context testSave = metricRegistry.timer(MetricRegistry.name(PerformanceTestServiceImpl.class, "testSave")).time();
        testSave("sync", 1);
        testSave.stop();

        com.codahale.metrics.Timer.Context testJMS = metricRegistry.timer(MetricRegistry.name(PerformanceTestServiceImpl.class, "testJMS")).time();
        testJMS();
        testJMS.stop();

        testDBSaveAndJMSInside.stop();
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 180)
    @Override
    public void testDBJMSCommit() {
        testJMS();
        LOG.info("JMS message sent");

        sleepInSeconds(30);

        testSave("sync", 1);

        LOG.info("Message saved in database");
    }

    protected void sleepInSeconds(long sleep) {
        LOG.info("Sleeping for [{}]", sleep);
        try {
            Thread.sleep(1000 * sleep);
        } catch (InterruptedException e) {
            LOG.error("Error sleeping", e);
        }
        LOG.info("Finished sleeping for [{}]", sleep);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void testJMS() {
        jmsManager.sendMessageToQueue(
                new NotifyMessageCreator(System.currentTimeMillis() + "", NotificationType.MESSAGE_RECEIVED, null).createMessage(),
                "jms/domibus.DLQ");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void testSave(String type, int count) {
        com.codahale.metrics.Timer.Context saveSyncTimerContext = metricRegistry.timer(MetricRegistry.name(PerformanceTestServiceImpl.class, "testOnlyDBInside")).time();

        byte[] payload = null;
        try {
            payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("50K_payload_SendMessage.xml"), "UTF-8").getBytes();
        } catch (IOException exc) {
            LOG.error("Exception ", exc);
        }

        List<TestMessage> rawMessages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TestMessage rawMessage = new TestMessage("[" + i + "]" + type + System.currentTimeMillis());
            rawMessage.setMessageId(i);
//            rawMessage.setRawPayload(payload);
            rawMessages.add(rawMessage);
        }

        rawMessages.forEach(el -> {
            if (type.equals("async")) {
                //LOG.debug("Saving async [{}]", rawMessages.indexOf(el));
//                saveAsync(el);
                //LOG.debug("Saved async [{}]", rawMessages.indexOf(el));
            } else {
                //LOG.info("Saving sync [{}]", rawMessages.indexOf(el));
                saveSync(el);
                //LOG.info("Saved sync [{}]", rawMessages.indexOf(el));
            }
        });
        saveSyncTimerContext.stop();
    }

    public void saveAsync(RawMessage rawMessage) {
        LOG.warn("Saving async [{}]", rawMessage.getMessageId());
        domainTaskExecutor.submit(() -> rawMessageService.saveSync(rawMessage, "SaveAsync-2"));
    }

    public void saveSync(TestMessage rawMessage) {
        rawMessageService.saveTestMessage(rawMessage);
    }
}
