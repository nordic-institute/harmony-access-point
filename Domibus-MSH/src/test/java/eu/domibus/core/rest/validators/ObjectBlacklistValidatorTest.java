package eu.domibus.core.rest.validators;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.web.rest.ro.MessageFilterRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ObjectBlacklistValidatorTest {
    @Tested
    ObjectBlacklistValidator blacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

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

    @Test()
    public void testInvalidMessageMultiple() throws InterruptedException {
        RoutingCriteria rt1 = new RoutingCriteria();
        rt1.setName("name1");
        rt1.setExpression("expression;");
        MessageFilterRO ro1 = new MessageFilterRO();
        ro1.setEntityId(1);
        ro1.setRoutingCriterias(Arrays.asList(rt1));

        RoutingCriteria rt2 = new RoutingCriteria();
        rt2.setName("name2%");
        rt2.setExpression("expression2");
        MessageFilterRO ro2 = new MessageFilterRO();
        ro2.setEntityId(2);
        ro2.setRoutingCriterias(Arrays.asList(rt2));

        new Expectations(blacklistValidator) {{
            domibusPropertyProvider.getProperty(FieldBlacklistValidator.BLACKLIST_PROPERTY);
            returns(";%'\\/");
        }};
        blacklistValidator.init();

        Thread t2 = new Thread(() -> {
            boolean actualValid2 = blacklistValidator.isValid(ro2);
            String mess2 = blacklistValidator.getErrorMessage();
            Assert.assertEquals("Forbidden character detected in property root->routingCriterias[1]->name", mess2);
        });


        boolean actualValid1 = blacklistValidator.isValid(ro1);
        t2.start();
        Thread.currentThread().sleep(1000);
        String mess1 = blacklistValidator.getErrorMessage();
        Assert.assertEquals("Forbidden character detected in property root->routingCriterias[1]->expression", mess1);
    }
}