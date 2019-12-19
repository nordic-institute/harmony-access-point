package eu.domibus.web.rest.testdb;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.metrics.MetricsHelper;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.common.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.submission.RawMessage;
import eu.domibus.submission.RawMessageService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.io.IOException;

import static eu.domibus.common.metrics.MetricNames.SERVLET_INCOMING_USER_MESSAGE;
import static eu.domibus.common.metrics.MetricNames.TEST_CN;

@RestController
@RequestMapping(value = "/rest/testconnection")

public class TestConnection {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestConnection.class);

    @Autowired
    RawMessageService rawMessageService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @RequestMapping(path = "cn", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResponseEntity<String> testCn(
            @RequestPart("myFile") MultipartFile messageXML,
            @DefaultValue("0") @QueryParam("sleep") int sleep,
            @DefaultValue("0") @QueryParam("sync") int sync ) { // 0 is save sync, 1 is use new Thread().start, 2 is use dte

        LOG.info("Testing the connection and db, sleep: [{}], sync [{}] ", sleep, sync);
        byte[] payload;
        Counter counter = null;
        com.codahale.metrics.Timer.Context testCnTimerContext = null;
        try {
            testCnTimerContext = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(TestDB.class, "testCn_timer")).time();
            counter = MetricsHelper.getMetricRegistry().counter(MetricRegistry.name(TestDB.class, "testCn_counter"));
            counter.inc();
            try {
                payload = messageXML.getBytes();
            } catch (IOException e) {
                LOG.error("!!!!!!!!!Exception getting bytes", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception getting bytes " + ExceptionUtils.getRootCauseMessage(e));
            }

            int l = payload.length;
            LOG.info("Message length is [{}], [{}]", l, payload[l - 1]);


            RawMessage rawMessage = new RawMessage("Save-" + sync + " " + System.currentTimeMillis());
            rawMessage.setMessageId((int) System.currentTimeMillis());
            rawMessage.setRawPayload(payload);

            if(sync == 0) {
                saveSync(rawMessage);
            } else if(sync == 1) {
                saveAsyncNew(rawMessage);
            } else if(sync == 2) {
                saveAsync(rawMessage);
            }

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep * 10);
                } catch (InterruptedException e) {
                    LOG.error("!!!!!!!!!Interrupted exception ", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interrupted exception " + ExceptionUtils.getRootCauseMessage(e));
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body("OK");
        } finally {
            if (counter != null) {
                counter.dec();
            }
            if (testCnTimerContext != null) {
                testCnTimerContext.stop();
            }
        }
    }


    public void saveAsync(RawMessage rawMessage) {
        LOG.warn("Saving async [{}]", rawMessage.getMessageId());
        domainTaskExecutor.submit(() -> rawMessageService.saveSync(rawMessage, "SaveAsync-2"));
    }

    public void saveAsyncNew(RawMessage rawMessage) {
        LOG.warn("Saving async new [{}]", rawMessage.getMessageId());

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                rawMessageService.saveSync(rawMessage, "SaveAsyncNew-3");
            }
        };
        new Thread(task).start();
    }

    public void saveSync(RawMessage rawMessage) {
        rawMessageService.saveSync(rawMessage, "SaveSync-1");
    }

}

