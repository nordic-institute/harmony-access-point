package eu.domibus.web.rest;

import eu.domibus.api.party.PartyService;
import eu.domibus.core.converter.PartyCoreMapper;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.monitoring.ConnectionMonitoringService;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.web.rest.ro.ConnectionMonitorRO;
import eu.domibus.web.rest.ro.TestServiceRequestRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RestController
@RequestMapping(value = "/rest/testservice")
@Validated
public class TestServiceResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TestServiceResource.class);

    @Autowired
    protected TestService testService;

    @Autowired
    private PartyService partyService;

    @Autowired
    protected ConnectionMonitoringService connectionMonitoringService;

    @Autowired
    PartyCoreMapper partyCoreMapper;

    @RequestMapping(value = "sender", method = RequestMethod.GET)
    public PartyResponseRo getSenderParty() {
        return partyCoreMapper.partyToPartyResponseRo(partyService.getGatewayParty());
    }

    @RequestMapping(value = "parties", method = RequestMethod.GET)
    public List<String> getTestParties() {
        List<String> pushToPartyNamesByServiceAndAction = partyService.findPushToPartyNamesForTest();
        return pushToPartyNamesByServiceAndAction;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String submitTest(@RequestBody @Valid TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return testService.submitTest(testServiceRequestRO.getSender(), testServiceRequestRO.getReceiver());
    }

    @RequestMapping(value = "dynamicdiscovery", method = RequestMethod.POST)
    @ResponseBody
    public String submitTestDynamicDiscovery(@RequestBody @Valid TestServiceRequestRO testServiceRequestRO) throws IOException, MessagingProcessingException {
        return testService.submitTestDynamicDiscovery(testServiceRequestRO.getSender(), testServiceRequestRO.getReceiver(), testServiceRequestRO.getReceiverType());
    }

    @RequestMapping(value = "connectionmonitor", method = RequestMethod.GET)
    public Map<String, ConnectionMonitorRO> getConnectionMonitorStatus(String senderPartyId, String[] partyIds) {
        Map<String, ConnectionMonitorRO> connectionStatus = connectionMonitoringService.getConnectionStatus(senderPartyId, partyIds);
        return connectionStatus;
    }
}
