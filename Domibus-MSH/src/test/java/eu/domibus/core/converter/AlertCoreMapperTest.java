package eu.domibus.core.converter;

import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.service.AbstractPropertyValue;
import eu.domibus.core.alerts.model.service.StringPropertyValue;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * @author François Gautier
 * @since 5.0
 */

public class AlertCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private AlertCoreMapper alertCoreMapper;

    private final LocalDate aLocalDate = LocalDate.MAX;

    @Test
    @Ignore("[EDELIVERY-8739] Test runs locally but fails on Bamboo")
    public void testConvertServiceAlertToPersistAlert() {
        eu.domibus.core.alerts.model.service.Alert toConvert = (eu.domibus.core.alerts.model.service.Alert) objectService.createInstance(eu.domibus.core.alerts.model.service.Alert.class);
        toConvert.getEvents().forEach(event -> {
            event.setLastAlertDate(aLocalDate);
            HashMap<String, AbstractPropertyValue> properties = new HashMap<>();
            properties.put("key", new StringPropertyValue("key", "value"));
            event.setProperties(properties);
        });

        final Alert converted = alertCoreMapper.alertServiceToAlertPersist(toConvert);
        final eu.domibus.core.alerts.model.service.Alert convertedBack = alertCoreMapper.alertPersistToAlertService(converted);
        // these fields are not present in convertedBack object, fill them so the assertion works
        convertedBack.setCreationTime(toConvert.getCreationTime());
        convertedBack.setProperties(toConvert.getProperties());
        convertedBack.setNextAttempt(toConvert.getNextAttempt());
        convertedBack.setNextAttemptOffsetSeconds(toConvert.getNextAttemptOffsetSeconds());
        convertedBack.setNextAttemptTimezoneId(toConvert.getNextAttemptTimezoneId());
        objectService.assertObjects(convertedBack, toConvert);
    }

}