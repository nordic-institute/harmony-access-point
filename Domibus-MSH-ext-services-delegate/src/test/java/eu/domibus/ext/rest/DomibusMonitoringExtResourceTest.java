package eu.domibus.ext.rest;

import eu.domibus.ext.domain.monitoring.MonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.services.DomibusMonitoringExtService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusMonitoringExtResourceTest {

    @Tested
    DomibusMonitoringExtResource domibusMonitoringExtResource;

    @Injectable
    DomibusMonitoringExtService domibusMonitoringExtService;

    @Test
    public void getDomibusStatusTest() throws DomibusMonitoringExtException {
        MonitoringInfoDTO monitoringInfoDTO = new MonitoringInfoDTO();
        List<String> filter = new ArrayList<>();
        filter.add("db");

        new Expectations() {{
            domibusMonitoringExtService.getMonitoringDetails(filter);
            result = monitoringInfoDTO;
        }};

        final MonitoringInfoDTO responseList = domibusMonitoringExtResource.getMonitoringDetails(filter);

        Assert.assertNotNull(responseList);
    }

}

