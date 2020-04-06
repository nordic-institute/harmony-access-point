package eu.domibus.web.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.rest.validators.ObjectBlacklistValidatorDelegate;
import eu.domibus.web.rest.ro.MessageFilterRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ObjectFieldBlacklistValidatorTest {
    @Tested
    ObjectBlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ObjectBlacklistValidatorDelegate objectBlacklistValidatorDelegate;

    @Test
    public void testValid() {
        RoutingCriteria rt = new RoutingCriteria();
        rt.setName("name");
        rt.setExpression("expression");
        rt.setEntityId(2);

        MessageFilterRO ro = new MessageFilterRO();
        ro.setPersisted(false);
        ro.setEntityId(1);
        ro.setBackendName("jms");
        ro.setIndex(2);
        ro.setRoutingCriterias(Arrays.asList(rt, rt));

        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns(";%'\\/");
        }};

        blacklistValidator.init();

        boolean actualValid = blacklistValidator.isValid(ro);

        Assert.assertEquals(true, actualValid);
    }

    @Test()
    public void testInvalid() {
        RoutingCriteria rt = new RoutingCriteria();
        rt.setName("name");
        rt.setExpression("expression;");
        rt.setEntityId(2);

        MessageFilterRO ro = new MessageFilterRO();
        ro.setPersisted(false);
        ro.setEntityId(1);
        ro.setBackendName("jms");
        ro.setIndex(2);
        ro.setRoutingCriterias(Arrays.asList(rt, rt));

        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns(";%'\\/");
        }};

        blacklistValidator.init();

        boolean actualValid = blacklistValidator.isValid(ro);

        Assert.assertEquals(false, actualValid);
    }
}