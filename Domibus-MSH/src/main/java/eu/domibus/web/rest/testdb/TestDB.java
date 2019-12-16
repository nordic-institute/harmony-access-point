package eu.domibus.web.rest.testdb;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.submission.RawMessage;
import eu.domibus.submission.RawMessageService;
import org.apache.cxf.helpers.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/testdb")
public class TestDB {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestDB.class);

    @Autowired
    RawMessageService rawMessageService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @GetMapping(path = "{type}")
    public void testSave(
            @PathVariable(value = "type") String type,
            @QueryParam("count") int count) {
        LOG.info("Testing the database with [{}] messages, type: [{}]", count, type);
        byte[] payload = null;
        try {
            payload = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("payload_SendMessage.xml"), "UTF-8").getBytes();
        } catch (IOException exc) {
            LOG.error("Exception ", exc);
        }

        List<RawMessage> rawMessages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RawMessage rawMessage = new RawMessage("[" + type + System.currentTimeMillis() + "] ");
            rawMessage.setRawPayload(payload);
            rawMessages.add(rawMessage);
        }

        rawMessages.forEach(el -> {
            if (type.equals("async")) {
                LOG.info("Saving async [{}]", rawMessages.indexOf(el));
                saveAsync(el);
                LOG.info("Saved async [{}]", rawMessages.indexOf(el));
            } else {
                LOG.info("Saving sync [{}]", rawMessages.indexOf(el));
                saveSync(el);
                LOG.info("Saved sync [{}]", rawMessages.indexOf(el));
            }
        });

    }

    public void saveAsync(RawMessage rawMessage) {
        domainTaskExecutor.submit(() -> rawMessageService.saveSync(rawMessage, "SaveAsync-2"));
    }

    public void saveSync(RawMessage rawMessage) {
        rawMessageService.saveSync(rawMessage, "SaveSync-1");
    }


}

