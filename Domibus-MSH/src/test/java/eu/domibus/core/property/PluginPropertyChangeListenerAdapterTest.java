package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PluginPropertyChangeListenerAdapterTest extends TestCase {

    @Tested
    PluginPropertyChangeListenerAdapter pluginPropertyChangeListenerAdapter;

    @Injectable
    PluginPropertyChangeListener pluginPropertyChangeListener;

    @Test
    public void handlesProperty(@Mocked String propertyName) {
        Boolean handles = true;
        new Expectations() {{
            pluginPropertyChangeListener.handlesProperty(propertyName);
            result = handles;
        }};

        boolean res = pluginPropertyChangeListenerAdapter.handlesProperty(propertyName);

        Assert.assertEquals(handles, res);
    }

    @Test
    public void propertyValueChanged_error(@Mocked String domainCode, @Mocked String propertyName, @Mocked String propertyValue,
                                           @Mocked DomibusPropertyException exception) {
        String errorMessage = "errorMessage";
        new Expectations() {{
            exception.getMessage();
            result = errorMessage;
            pluginPropertyChangeListener.propertyValueChanged(domainCode, propertyName, propertyValue);
            result = exception;
        }};

        try {
            pluginPropertyChangeListenerAdapter.propertyValueChanged(domainCode, propertyName, propertyValue);
            Assert.fail();
        } catch (DomibusPropertyExtException ex) {
            Assert.assertEquals(ex.getCause(), exception);
            Assert.assertTrue(ex.getMessage().contains(errorMessage));
        }
    }

    @Test
    public void propertyValueChanged_ok(@Mocked String domainCode, @Mocked String propertyName, @Mocked String propertyValue) {
        pluginPropertyChangeListenerAdapter.propertyValueChanged(domainCode, propertyName, propertyValue);

        new Verifications() {{
            pluginPropertyChangeListener.propertyValueChanged(domainCode, propertyName, propertyValue);
        }};

    }
}