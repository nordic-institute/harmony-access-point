package eu.domibus.core.message.retention;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
//import eu.domibus.core.alerts.configuration.partitions.PartitionsConfigurationManager;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PartitionServiceTest {

    @Tested
    PartitionService partitionsService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    EventService eventService;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    AlertConfigurationService alertConfigurationService;

//    @Injectable
//    PartitionsConfigurationManager partitionsConfigurationManager;

    @Test
    public void verifyPartitionsInAdvance() {

        new Expectations() {{
            userMessageDao.checkPartitionExists(anyString);
            result = true;
        }};

        partitionsService.verifyPartitionsInAdvance();

        new Verifications() {{
//            eventService.enqueuePartitionCheckEvent(anyString);
            eventService.enqueueEvent(EventType.PARTITION_CHECK, anyString, new EventProperties(anyString));
            times = 0;
        }};
    }

    @Test
    public void verifyPartitionsInAdvanceNotOk() {

        new Expectations() {{
            userMessageDao.checkPartitionExists(anyString);
            result = false;

//            partitionsConfigurationManager.getConfiguration().isActive();
            alertConfigurationService.getConfiguration(AlertType.PARTITION_CHECK).isActive();
            result = true;
        }};

        partitionsService.verifyPartitionsInAdvance();

        new Verifications() {{
//            eventService.enqueuePartitionCheckEvent(anyString);
            eventService.enqueueEvent(EventType.PARTITION_CHECK, anyString, new EventProperties(anyString));
            times = 1;
        }};
    }
}
