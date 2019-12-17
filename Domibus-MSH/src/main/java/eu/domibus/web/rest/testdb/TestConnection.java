package eu.domibus.web.rest.testdb;

import eu.domibus.common.metrics.Counter;
import eu.domibus.common.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.QueryParam;
import java.io.IOException;

import static eu.domibus.common.metrics.MetricNames.SERVLET_INCOMING_USER_MESSAGE;
import static eu.domibus.common.metrics.MetricNames.TEST_CN;

@RestController
@RequestMapping(value = "/rest/testconnection")

public class TestConnection {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestConnection.class);

    @RequestMapping(path = "cn", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Timer(value = TEST_CN)
    @Counter(TEST_CN)
    public ResponseEntity<String> testCn(
            @RequestPart("myFile") MultipartFile messageXML,
            @QueryParam("count") int count) {
        LOG.info("Testing the connection, count: [{}]", count);
        byte[] payload = null;
        try {
            payload = messageXML.getBytes();
        } catch (IOException e) {
            LOG.error("!!!!!!!!!Exception getting bytes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception getting bytes " + ExceptionUtils.getRootCauseMessage(e));
        }

        int l = payload.length;
        LOG.info("Message length is [{}], [{}]", l, payload[l-1]);

        if (count > 0) {
            try {
                Thread.sleep(count*1000);
            } catch (InterruptedException e) {
                LOG.error("!!!!!!!!!Interrupted exception ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interrupted exception " + ExceptionUtils.getRootCauseMessage(e));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}

