package eu.domibus.api.property.validators;

import org.quartz.CronExpression;

public class CronValidator implements DomibusPropertyValidator {
    public boolean isValid(String propValue) {
        return CronExpression.isValidExpression(propValue);
    }
}
