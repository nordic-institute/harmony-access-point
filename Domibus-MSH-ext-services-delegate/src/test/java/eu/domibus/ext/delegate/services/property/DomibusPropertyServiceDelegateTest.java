package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.NotificationType;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.services.DomainContextExtService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    protected DomainExtConverter domainConverter;

    @Injectable
    DomainContextExtService domainContextService;

    @Test
    public void getConfiguredNotifications() {
        String propertyName = "messages.notifications";
        List<NotificationType> expectedTypes = Arrays.asList(new NotificationType[]{NotificationType.MESSAGE_RECEIVED, NotificationType.MESSAGE_SEND_SUCCESS});
        String propertyValue = StringUtils.join(expectedTypes.stream().map(notificationType -> notificationType.toString()).collect(Collectors.toList()), ",");

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
        List<NotificationType> expectedTypes = Arrays.asList(new NotificationType[]{NotificationType.MESSAGE_RECEIVED, NotificationType.MESSAGE_SEND_SUCCESS});
        List<NotificationType> configuredValues = new ArrayList<>();
        configuredValues.addAll(expectedTypes);
        //duplicate values
        configuredValues.addAll(expectedTypes);

        String propertyValue = StringUtils.join(expectedTypes.stream().map(notificationType -> notificationType.toString()).collect(Collectors.toList()), ",");

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
        assertEquals(notificationType, NotificationType.MESSAGE_RECEIVED);
    }

    @Test
    public void getNotificationTypeWithInvalidValue() {
        assertNull(domibusPropertyServiceDelegate.getNotificationType("invalid value"));
    }

}