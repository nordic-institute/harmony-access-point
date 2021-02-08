package eu.domibus.core.message;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT;

/**
 * @author Ion Perpegel
 * @since 4.2.1
 */
@RunWith(JMockit.class)
public class MessagesLogServiceHelperImplTest extends TestCase {

    @Tested
    MessagesLogServiceHelperImpl messagesLogServiceHelper;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void calculateNumberOfMessages_NotEstimated(@Injectable MessageLogDaoBase dao, @Mocked Map<String, Object> filters) {
        long count = 100;
        MessageLogResultRO resultRO = new MessageLogResultRO();

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT);
            result = 0;
            dao.countEntries(filters);
            result = count;
        }};

        long result = messagesLogServiceHelper.calculateNumberOfMessages(dao, filters, resultRO);

        new Verifications() {{
            dao.countEntriesWithLimit(filters, anyInt);
            times = 0;
        }};

        Assert.assertEquals(count, result);
        Assert.assertEquals(count, (long)resultRO.getCount());
        Assert.assertEquals(false, resultRO.isEstimatedCount());
    }

    @Test
    public void calculateNumberOfMessages_Estimated(@Injectable MessageLogDaoBase dao, @Mocked Map<String, Object> filters) {
        long count = 1000;
        int limit = 100;
        MessageLogResultRO resultRO = new MessageLogResultRO();

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_MESSAGE_LOGS_COUNT_LIMIT);
            this.result = limit;
            dao.countEntriesWithLimit(filters, anyInt);
            result=limit;
        }};

        long result = messagesLogServiceHelper.calculateNumberOfMessages(dao, filters, resultRO);

        new Verifications() {{
            dao.countEntries(filters);
            times = 0;
        }};

        Assert.assertEquals(limit, result);
        Assert.assertEquals(limit, (long)resultRO.getCount());
        Assert.assertEquals(true, resultRO.isEstimatedCount());
    }
}