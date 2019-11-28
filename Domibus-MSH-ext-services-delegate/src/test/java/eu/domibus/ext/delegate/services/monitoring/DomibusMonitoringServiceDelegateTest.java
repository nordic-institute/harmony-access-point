package eu.domibus.ext.delegate.services.monitoring;

import eu.domibus.api.monitoring.*;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusMonitoringInfoDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(JMockit.class)
public class DomibusMonitoringServiceDelegateTest {

    @Tested
    DomibusMonitoringServiceDelegate domibusMonitoringServiceDelegate;

    @Injectable
    DomibusMonitoringService domibusMonitoringService;
    ;

    @Injectable
    DomainExtConverter domainConverter;

    @Test
    public void getDomibusStatusTest() {
        DataBaseInfo dataBaseInfo = new DataBaseInfo();
        dataBaseInfo.setName("Database");
        dataBaseInfo.setStatus(MonitoringStatus.NORMAL);
        JmsBrokerInfo jmsBrokerInfo = new JmsBrokerInfo();
        jmsBrokerInfo.setName("JMS Broker");
        jmsBrokerInfo.setStatus(MonitoringStatus.NORMAL);
        DomibusMonitoringInfo domibusMonitoringInfo = new DomibusMonitoringInfo();
        List<String> filter = new ArrayList<>();
        filter.add("db");

        new Expectations() {{
            domibusMonitoringService.getDomibusStatus(filter);
            result = domibusMonitoringInfo;
        }};

        // When
        domibusMonitoringServiceDelegate.getDomibusStatus(filter);

        // Then
        new Verifications() {{
            domibusMonitoringService.getDomibusStatus(filter);
            domainConverter.convert(domibusMonitoringInfo, DomibusMonitoringInfoDTO.class);
        }};

    }

    @Test
    public void testDomibusMonitoringExtException() {
        // Given
        final DomibusMonitoringExtException domibusMonitoringExtException = new DomibusMonitoringExtException(DomibusErrorCode.DOM_001, "test");
        List<String> filter = new ArrayList<>();
        filter.add("db");
        new Expectations() {{
            domibusMonitoringService.getDomibusStatus(filter);
            result = domibusMonitoringExtException;
        }};

        // When
        try {
            domibusMonitoringServiceDelegate.getDomibusStatus(filter);
        } catch (DomibusMonitoringExtException e) {
            // Then
            Assert.assertTrue(domibusMonitoringExtException == e);
            return;
        }
        Assert.fail();
    }
}
