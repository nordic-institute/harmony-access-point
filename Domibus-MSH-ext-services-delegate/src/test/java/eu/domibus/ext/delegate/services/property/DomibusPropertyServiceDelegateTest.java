package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.delegate.mapper.DomibusExtMapper;
import eu.domibus.ext.services.DomainContextExtService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusPropertyServiceDelegateTest {

    @Tested
    DomibusPropertyServiceDelegate domibusPropertyServiceDelegate;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomibusExtMapper domibusExtMapper;

    @Injectable
    DomainContextExtService domainContextService;

    @Test
    public void getConfiguredNotifications() {
        String propertyName = "messages.notifications";
        List<NotificationType> expectedTypes = Arrays.asList(NotificationType.MESSAGE_RECEIVED, NotificationType.MESSAGE_SEND_SUCCESS);
        String propertyValue = StringUtils.join(expectedTypes.stream().map(Enum::toString).collect(Collectors.toList()), ",");

        new Expectations(domibusPropertyServiceDelegate) {{
            domibusPropertyServiceDelegate.getProperty(propertyName);
            result = propertyValue;
        }};

        List<NotificationType> configuredNotifications = domibusPropertyServiceDelegate.getConfiguredNotifications(propertyName);
        assertEquals(expectedTypes, configuredNotifications);
    }

    @Test
    public void getConfiguredNotificationsWithDuplicateValues() {
        String propertyName = "messages.notifications";
        List<NotificationType> expectedTypes = Arrays.asList(NotificationType.MESSAGE_RECEIVED, NotificationType.MESSAGE_SEND_SUCCESS);
        String propertyValue = StringUtils.join(expectedTypes.stream().map(Enum::toString).collect(Collectors.toList()), ",");

        new Expectations(domibusPropertyServiceDelegate) {{
            domibusPropertyServiceDelegate.getProperty(propertyName);
            result = propertyValue;
        }};

        List<NotificationType> configuredNotifications = domibusPropertyServiceDelegate.getConfiguredNotifications(propertyName);
        assertEquals(expectedTypes, configuredNotifications);
    }

    @Test
    public void getConfiguredNotificationsWithInvalidValues() {
        String propertyName = "messages.notifications";
        String propertyValue = "invalid notif type,,";

        new Expectations(domibusPropertyServiceDelegate) {{
            domibusPropertyServiceDelegate.getProperty(propertyName);
            result = propertyValue;
        }};

        List<NotificationType> configuredNotifications = domibusPropertyServiceDelegate.getConfiguredNotifications(propertyName);
        assertTrue(CollectionUtils.isEmpty(configuredNotifications));
    }

    @Test
    public void getNotificationType() {
        NotificationType notificationType = domibusPropertyServiceDelegate.getNotificationType(NotificationType.MESSAGE_RECEIVED.toString());
        assertEquals(NotificationType.MESSAGE_RECEIVED, notificationType);
    }

    @Test
    public void getNotificationTypeWithInvalidValue() {
        assertNull(domibusPropertyServiceDelegate.getNotificationType("invalid value"));
    }

}