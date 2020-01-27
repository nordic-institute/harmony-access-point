package eu.domibus.ext.rest;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.metrics.MetricsHelper;
import eu.domibus.api.performancetest.PerformanceTestService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.QueryParam;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RestController
@RequestMapping(value = "/ext/performancetest")
public class PerformanceTestResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PerformanceTestResource.class);

    @Autowired
    protected PerformanceTestService performanceTestService;

    @Autowired
    protected MetricRegistry metricRegistry;

    @Autowired
    protected JMSManager jmsManager;

    @GetMapping(path = "xa")
    public void testXA() {
        com.codahale.metrics.Timer.Context saveSyncTimerContext = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testDBSaveAndJMSOutside")).time();
        performanceTestService.testDBSaveAndJMS();
        saveSyncTimerContext.stop();
    }

    @GetMapping(path = "onlydb")
    public void testSaveOnlyDB(@QueryParam("mode") String mode) {
        if("all".equalsIgnoreCase(mode)) {
            com.codahale.metrics.Timer.Context saveSyncTimerContext = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testOnlyDBSeparate")).time();
            performanceTestService.testSave("sync", 1);
            performanceTestService.testSave("sync", 1);
            performanceTestService.testSave("sync", 1);
            saveSyncTimerContext.stop();
        } else {
            com.codahale.metrics.Timer.Context saveSyncTimerContext = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testOnlyDBOneTransaction")).time();
            performanceTestService.testSave("sync", 3);
            saveSyncTimerContext.stop();
        }

    }

    @GetMapping(path = "jmscommit")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testJMSCommit() {
        performanceTestService.testDBJMSCommit();
    }

    @GetMapping(path = "xawithseparatetransactions")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testXAWithseparatetransactions() {
        com.codahale.metrics.Timer.Context saveSyncTimerContext = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testDBSaveAndJMSOutsideSeparate")).time();
        com.codahale.metrics.Timer.Context testSave = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testSaveSeparate")).time();
        performanceTestService.testSave("sync", 1);
        testSave.stop();

        com.codahale.metrics.Timer.Context testJMS = metricRegistry.timer(MetricRegistry.name(PerformanceTestResource.class, "testJMSSeparate")).time();
        performanceTestService.testJMS();
        testJMS.stop();
        saveSyncTimerContext.stop();
    }

    @GetMapping(path = "db/{type}")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testSave(
            @PathVariable(value = "type") String type,
            @QueryParam("count") int count) {
        LOG.info("Testing the database with [{}] messages, type: [{}]", count, type);

        performanceTestService.testSave(type, count);
    }

    @RequestMapping(path = "cn", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResponseEntity<String> testCn(
            @RequestPart("myFile") MultipartFile messageXML,
            @QueryParam("count") int count) {
        LOG.info("Testing the connection, count: [{}]", count);
        byte[] payload = null;
        Counter counter = null;
        com.codahale.metrics.Timer.Context testCnTimerContext = null;
        try {
            testCnTimerContext = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(PerformanceTestResource.class, "testCn_timer")).time();
            counter = MetricsHelper.getMetricRegistry().counter(MetricRegistry.name(PerformanceTestResource.class, "testCn_counter"));
            counter.inc();
            try {
                payload = messageXML.getBytes();
            } catch (IOException e) {
                LOG.error("!!!!!!!!!Exception getting bytes", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception getting bytes " + ExceptionUtils.getRootCauseMessage(e));
            }

            int l = payload.length;
            LOG.info("Message length is [{}], [{}]", l, payload[l - 1]);

            if (count > 0) {
                try {
                    Thread.sleep(count * 10);
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


}
