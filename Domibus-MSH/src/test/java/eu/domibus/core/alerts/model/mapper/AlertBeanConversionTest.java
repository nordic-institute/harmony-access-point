package eu.domibus.core.alerts.model.mapper;

import eu.domibus.core.alerts.model.common.AccountEventKey;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Thomas Dussart, Ioana Dragusanu
 * @since 4.0
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class AlertBeanConversionTest {

    @Configuration
    static class ContextConfiguration {
    }

    @Test
    public void testConversion() throws ParseException {
        final String user = "user";
        final String accountDisabled = "false";

        SimpleDateFormat parser = new SimpleDateFormat("dd/mm/yyy HH:mm:ss");
        final Date reportingTime = parser.parse("25/10/2001 00:00:00");
        final Date loginTime = parser.parse("26/10/2001 00:00:00");


        Event event = new Event();
        event.setEntityId(1);
        event.setType(EventType.USER_LOGIN_FAILURE);
        event.setReportingTime(reportingTime);

        event.addDateKeyValue(AccountEventKey.LOGIN_TIME.name(), loginTime);
        event.addStringKeyValue(AccountEventKey.USER.name(), user);
        event.addStringKeyValue(AccountEventKey.ACCOUNT_DISABLED.name(), accountDisabled);

        EventMapperImpl eventMapper = new EventMapperImpl();
        eventMapper.setDelegate(new EventMapperImpl_());
        final eu.domibus.core.alerts.model.persist.Event persistEvent = eventMapper.eventServiceToEventPersist(event);
        Assert.assertEquals(1, persistEvent.getEntityId());
        Assert.assertEquals(EventType.USER_LOGIN_FAILURE, persistEvent.getType());
        Assert.assertEquals(reportingTime, persistEvent.getReportingTime());

        Assert.assertEquals(loginTime, persistEvent.getProperties().get(AccountEventKey.LOGIN_TIME.name()).getValue());
        Assert.assertEquals(user, persistEvent.getProperties().get(AccountEventKey.USER.name()).getValue());
        Assert.assertEquals(accountDisabled, persistEvent.getProperties().get(AccountEventKey.ACCOUNT_DISABLED.name()).getValue());
    }
}
