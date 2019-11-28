package eu.domibus.ext.rest;

import eu.domibus.ext.domain.DomibusMonitoringInfoDTO;
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
public class DomibusMonitoringResourceTest {

    @Tested
    DomibusMonitoringResource domibusMonitoringResource;

    @Injectable
    DomibusMonitoringExtService domibusMonitoringExtService;

    @Test
    public void getDomibusStatusTest() throws DomibusMonitoringExtException {
        DomibusMonitoringInfoDTO domibusMonitoringInfoDTO = new DomibusMonitoringInfoDTO();
        List<String> filter = new ArrayList<>();
        filter.add("db");

        new Expectations() {{
            domibusMonitoringExtService.getDomibusStatus(filter);
            result = domibusMonitoringInfoDTO;
        }};

        final DomibusMonitoringInfoDTO responseList = domibusMonitoringResource.getDomibusStatus(filter);

        Assert.assertNotNull(responseList);
    }

}

