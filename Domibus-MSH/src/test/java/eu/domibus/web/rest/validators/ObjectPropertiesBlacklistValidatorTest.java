package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.web.rest.ro.AlertFilterRequestRO;
import eu.domibus.web.rest.ro.AuditFilterRequestRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class ObjectPropertiesBlacklistValidatorTest {
    @Tested
    ObjectPropertiesBlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void testIsValid() {
        AlertFilterRequestRO ro = new AlertFilterRequestRO();
        ro.setColumn("col1");
        ro.setAlertType("ALERT_TYPE");
        ro.setParameters(new String[]{"param-1", "param.2"});

        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.BLACKLIST_PROPERTY);
            returns(";%'\\/");
        }};

        blacklistValidator.init();

        boolean actual = blacklistValidator.isValid(ro);

        Assert.assertEquals(true, actual);
    }

    @Test()
    public void testIsValidInvalid() {
        AuditFilterRequestRO ro = new AuditFilterRequestRO();
        ro.setMax(100);
        ro.setAction(new HashSet<>(Arrays.asList("action1", "action2")));
        ro.setAuditTargetName(new HashSet<>(Arrays.asList("messageLog", "%jmsFilter;")));

        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(BlacklistValidator.BLACKLIST_PROPERTY);
            returns(";%'\\/");
        }};

        blacklistValidator.init();

        boolean actual = blacklistValidator.isValid(ro);

        Assert.assertEquals(false, actual);
    }
}